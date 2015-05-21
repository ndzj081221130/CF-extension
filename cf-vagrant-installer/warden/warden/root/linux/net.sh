#!/bin/bash

[ -n "$DEBUG" ] && set -o xtrace
set -o nounset
set -o errexit
shopt -s nullglob

filter_forward_chain="warden-forward"
filter_default_chain="warden-default"
filter_instance_prefix="warden-instance-"
nat_prerouting_chain="warden-prerouting"
nat_postrouting_chain="warden-postrouting"
nat_instance_prefix="warden-instance-"

# Default ALLOW_NETWORKS/DENY_NETWORKS to empty
ALLOW_NETWORKS=${ALLOW_NETWORKS:-}
DENY_NETWORKS=${DENY_NETWORKS:-}

function external_ip() {
  # The ';tx;d;:x' trick deletes non-matching lines
  ip route get 8.8.8.8 | sed 's/.*src\s\(.*\)\s/\1/;tx;d;:x'
}

function teardown_deprecated_rules() {
  # Remove jump to warden-dispatch from INPUT
  /sbin/iptables -S INPUT 2> /dev/null |
    grep " -j warden-dispatch" |
    sed -e "s/-A/-D/" -e "s/\s\+\$//" |
    xargs --no-run-if-empty --max-lines=1 /sbin/iptables

  # Remove jump to warden-dispatch from FORWARD
  /sbin/iptables -S FORWARD 2> /dev/null |
    grep " -j warden-dispatch" |
    sed -e "s/-A/-D/" -e "s/\s\+\$//" |
    xargs --no-run-if-empty --max-lines=1 /sbin/iptables

  # Prune warden-dispatch
  /sbin/iptables -F warden-dispatch 2> /dev/null || true

  # Delete warden-dispatch
  /sbin/iptables -X warden-dispatch 2> /dev/null || true
}

function teardown_filter() {
  teardown_deprecated_rules

  # Prune warden-forward chain
  /sbin/iptables -S ${filter_forward_chain} 2> /dev/null |
    grep "\-g ${filter_instance_prefix}" |
    sed -e "s/-A/-D/" -e "s/\s\+\$//" |
    xargs --no-run-if-empty --max-lines=1 /sbin/iptables

  # Prune per-instance chains
  /sbin/iptables -S 2> /dev/null |
    grep "^-A ${filter_instance_prefix}" |
    sed -e "s/-A/-D/" -e "s/\s\+\$//" |
    xargs --no-run-if-empty --max-lines=1 /sbin/iptables

  # Delete per-instance chains
  /sbin/iptables -S 2> /dev/null |
    grep "^-N ${filter_instance_prefix}" |
    sed -e "s/-N/-X/" -e "s/\s\+\$//" |
    xargs --no-run-if-empty --max-lines=1 /sbin/iptables

  # Remove jump to warden-forward from FORWARD
  /sbin/iptables -S FORWARD 2> /dev/null |
    grep " -j ${filter_forward_chain}" |
    sed -e "s/-A/-D/" -e "s/\s\+\$//" |
    xargs --no-run-if-empty --max-lines=1 /sbin/iptables

  /sbin/iptables -F ${filter_forward_chain} 2> /dev/null || true
  /sbin/iptables -F ${filter_default_chain} 2> /dev/null || true
}

function setup_filter() {
  teardown_filter

  # Create or flush forward chain
  /sbin/iptables -N ${filter_forward_chain} 2> /dev/null || /sbin/iptables -F ${filter_forward_chain}
  /sbin/iptables -A ${filter_forward_chain} -j DROP

  # Create or flush default chain
  /sbin/iptables -N ${filter_default_chain} 2> /dev/null || /sbin/iptables -F ${filter_default_chain}

  # Always allow established connections to warden containers
  /sbin/iptables -A ${filter_default_chain} -m conntrack --ctstate ESTABLISHED,RELATED -j ACCEPT

  for n in ${ALLOW_NETWORKS}; do
    if [ "$n" == "" ]
    then
      break
    fi

    /sbin/iptables -A ${filter_default_chain} --destination "$n" --jump RETURN
  done

  for n in ${DENY_NETWORKS}; do
    if [ "$n" == "" ]
    then
      break
    fi

    /sbin/iptables -A ${filter_default_chain} --destination "$n" --jump DROP
  done

  /sbin/iptables -A FORWARD -i w-+ --jump ${filter_forward_chain}
}

function teardown_nat() {
  # Prune prerouting chain
  /sbin/iptables -t nat -S ${nat_prerouting_chain} 2> /dev/null |
    grep "\-j ${nat_instance_prefix}" |
    sed -e "s/-A/-D/" -e "s/\s\+\$//" |
    xargs --no-run-if-empty --max-lines=1 /sbin/iptables -t nat

  # Prune per-instance chains
  /sbin/iptables -t nat -S 2> /dev/null |
    grep "^-A ${nat_instance_prefix}" |
    sed -e "s/-A/-D/" -e "s/\s\+\$//" |
    xargs --no-run-if-empty --max-lines=1 /sbin/iptables -t nat

  # Delete per-instance chains
  /sbin/iptables -t nat -S 2> /dev/null |
    grep "^-N ${nat_instance_prefix}" |
    sed -e "s/-N/-X/" -e "s/\s\+\$//" |
    xargs --no-run-if-empty --max-lines=1 /sbin/iptables -t nat

  # Flush prerouting chain
  /sbin/iptables -t nat -F ${nat_prerouting_chain} 2> /dev/null || true

  # Flush postrouting chain
  /sbin/iptables -t nat -F ${nat_postrouting_chain} 2> /dev/null || true
}

function setup_nat() {
  teardown_nat

  # Create prerouting chain
  /sbin/iptables -t nat -N ${nat_prerouting_chain} 2> /dev/null || true

  # Bind chain to PREROUTING
  (/sbin/iptables -t nat -S PREROUTING | grep -q "\-j ${nat_prerouting_chain}\b") ||
    /sbin/iptables -t nat -A PREROUTING \
      --jump ${nat_prerouting_chain}

  # Bind chain to OUTPUT (for traffic originating from same host)
  (/sbin/iptables -t nat -S OUTPUT | grep -q "\-j ${nat_prerouting_chain}\b") ||
    /sbin/iptables -t nat -A OUTPUT \
      --out-interface "lo" \
      --jump ${nat_prerouting_chain}

  # Create postrouting chain
  /sbin/iptables -t nat -N ${nat_postrouting_chain} 2> /dev/null || true

  # Bind chain to POSTROUTING
  (/sbin/iptables -t nat -S POSTROUTING | grep -q "\-j ${nat_postrouting_chain}\b") ||
    /sbin/iptables -t nat -A POSTROUTING \
      --jump ${nat_postrouting_chain}

  # Enable NAT for traffic coming from containers
  (/sbin/iptables -t nat -S ${nat_postrouting_chain} | grep -q "\-j SNAT\b") ||
    /sbin/iptables -t nat -A ${nat_postrouting_chain} \
      --source ${POOL_NETWORK} \
      --jump SNAT \
      --to $(external_ip)
}

case "${1}" in
  setup)
    setup_filter
    setup_nat

    # Enable forwarding
    echo 1 > /proc/sys/net/ipv4/ip_forward
    ;;
  teardown)
    teardown_filter
    teardown_nat
    ;;
  *)
    echo "Unknown command: ${1}" 1>&2
    exit 1
    ;;
esac
