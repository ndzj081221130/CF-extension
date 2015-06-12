module LanguagePack::DatabaseHelpers
#http://search.maven.org/remotecontent?filepath=mysql/mysql-connector-java/5.1.12
  SERVICE_DRIVER_HASH = {
    "*mysql-connector-java-*.jar" =>
        "http://114.212.85.89:80/mysql-connector-java-5.1.12.jar",
    "*postgresql-*.jdbc*.jar" =>
        "http://114.212.85.89:80/postgresql-9.0-801.jdbc4.jar"
  }.freeze
#search.maven.org/remotecontent?filepath=postgresql/postgresql/9.0-801.jdbc4
  def install_database_drivers
    added_jars = []
    Dir.chdir("lib") do
      SERVICE_DRIVER_HASH.each_pair do |search_pattern, url|
         unless !Dir.glob(search_pattern).empty?
           fetch_package(File.basename(url), File.dirname(url))
           added_jars << File.basename(url)
         end
      end
    end
    added_jars
  end
end
