require "language_pack/java"
require "fileutils"
require 'find'
module LanguagePack
  class Tuscany < Java
    include LanguagePack::PackageFetcher
    # IP = "http://114.212.84.248/"
    DOWNLOAD_HOST = "http://114.212.84.248:80".freeze
    TUSCANY_VERSION = "tuscany-sca-2.0.1".freeze
    TUSCANY_PACKAGE = "#{TUSCANY_VERSION}.tar.gz".freeze
    TUSCANY_DOWNLOAD = "#{DOWNLOAD_HOST}/#{TUSCANY_PACKAGE}"
    
    MAVEN_VERSION = "apache-maven-3.0.5".freeze
    MAVEN_PACKAGE = "#{MAVEN_VERSION}.tar.gz".freeze
    MAVEN_DOWNLOAD = "#{DOWNLOAD_HOST}/#{MAVEN_PACKAGE}"
    
    def self.use?
       
      Dir.glob("**/*.composite").any?
    end
    
    def name
      "Tuscany 2.0"
    end
    
   
    def compile
      # `sudo su`
      Dir.chdir(build_path) do
        install_java
        install_tuscany
       # puts "port = #{$PORT}"
        
       # remove_tuscany_files
        
       
        
        setup_profiled
        setup_profiled_tuscany
         
      end
    end
    
    def tuscany_dir
      ".tuscany"
    end
    
    def maven_dir
      ".mvn"
    end
    
    def install_tuscany
      FileUtils.mkdir_p tuscany_dir
      tuscany_tarball = "#{tuscany_dir}/#{TUSCANY_PACKAGE}"
      download_tuscany tuscany_tarball
      puts "Unpacking Tuscany to #{tuscany_dir}"
      
      run_with_err_output("tar zxf #{tuscany_tarball} -C #{tuscany_dir}")
      
      FileUtils.rm_rf tuscany_tarball
      unless File.exists?("#{tuscany_dir}/#{TUSCANY_VERSION}/bin/tuscany.sh")
        puts "unable to retrieve Tuscany"
        exit 1
      end
    end
    
    def install_maven
      FileUtils.mkdir_p maven_dir
      maven_tarball = "#{maven_dir}/#{MAVEN_PACKAGE}"
      download_maven maven_tarball
      puts "Unpacking Maven to #{maven_dir}"
      
      run_with_err_output("tar zxf #{maven_tarball} -C #{maven_dir}")
      
      FileUtils.rm_rf maven_tarball
      unless File.exists?("#{maven_dir}/#{MAVEN_VERSION}/bin/mvn")
        puts "unable to retrieve Maven"
        exit 1
      end
      
    end
    
    def download_tuscany(tuscany_tarball)
      puts "Downloading Tuscany: #{TUSCANY_PACKAGE}"
      fetch_package TUSCANY_PACKAGE
      FileUtils.mv TUSCANY_PACKAGE , tuscany_tarball
    end
    
    def download_maven(maven_tarball)
      puts "Downloading Maven : #{MAVEN_PACKAGE}"
      fetch_package MAVEN_PACKAGE
      FileUtils.mv MAVEN_PACKAGE , maven_tarball
    end
    
    # def move_tuscany_to_root
      # run_with_err_output("mv #{tuscany_dir}/#{TUSCANY_VERSION}/* . && rm -rf #{tuscany_dir}")  
    # end
    
    def rewrite_composite_file
      Dir.chdir("#{build_path}/src/main/resources") do
        
        Find.find("#{build_path}/src/main/resources/") do |file|
          
          
          if (/composite\Z/ =~ file )
            fh = File.new("temp.composite","w+")
            str = IO.read(file) 
            
            
             
             
            
            fh.puts  str.gsub(/{{PORT}}/,'$PORT')
            fh.close
            
            File.delete(file)
            run_with_err_output("echo $PORT")
            
            run_with_err_output("echo $VCAP_CONSOLE_PORT")
            run_with_err_output("echo $VCAP_APP_HOST")
            
            run_with_err_output("mv temp.composite #{file}")
          end
        end
      end
      
    end
    
    
     def re_mvn_clean_install
       puts "mvn clean & install"
       run_with_err_output("#{maven_dir}/#{MAVEN_VERSION}/bin/mvn clean && #{maven_dir}/#{MAVEN_VERSION}/bin/mvn install -DskipTests")
     end
     
     
     def java_opts
      # TODO proxy settings?
      # Don't override 's temp dir setting
      opts = super.merge({ "-Dhttp.port=" => "$PORT" })
      #opts = super
      opts.delete("-Djava.io.tmpdir=")
      #puts "opts : #{opts}"
      opts
    end
    
    def default_process_types
      {
        "web" => "tuscany.sh /home/vcap/app" # /home/vcap/app/circle.sh" #
      }
    end 
      
      def setup_profiled_tuscany
        
        File.open("#{build_path}/.profile.d/tuscany.sh", "a") { |file| file.puts(bash_script_tuscany) }  
      
      end
      
      def setup_profiled_maven
        File.open("#{build_path}/.profile.d/maven.sh","a"){|file| file.puts(bash_script_maven)}
      end
      
      private 
    def bash_script_tuscany
      <<-BASH
#!/bin/bash
export TUSCANY_HOME="$HOME/#{tuscany_dir}/#{TUSCANY_VERSION}"
export PATH="$HOME/#{tuscany_dir}/#{TUSCANY_VERSION}/bin:$PATH" 
 
      BASH
    end
    
    def bash_script_maven
      <<-BASH
#!/bin/bash
export MAVEN_HOME="$HOME/#{maven_dir}/#{MAVEN_VERSION}"
export PATH="$HOME/#{maven_dir}/#{MAVEN_VERSION}/bin:$PATH"      
      BASH
      
    end
    
    
  end # end of class
  
end # end of module