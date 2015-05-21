#!/bin/bash
export HOME="$PWD/app"                                                          
export MEMORY_LIMIT="512m"                                                      
export PORT="$VCAP_APP_PORT"                                                    
export TMPDIR="$PWD/tmp"                                                        
unset GEM_PATH                                                                  
if [ -d app/.profile.d ]; then                                                  
  for i in app/.profile.d/*.sh; do                                              
    if [ -r $i ]; then                                                          
      . $i                                                                      
    fi                                                                          
  done                                                                          
  unset i                                                                       
fi                                                                              
env > logs/env.log                                                              
DROPLET_BASE_DIR=$PWD                                                           
cd app                                                                          
(tuscany.sh /home/vcap/app) > $DROPLET_BASE_DIR/logs/stdout.log 2> $DROPLET_BASE_DIR/logs/stderr.log &
STARTED=$!                                                                      
echo "$STARTED" >> $DROPLET_BASE_DIR/run.pid                                    
wait $STARTED
