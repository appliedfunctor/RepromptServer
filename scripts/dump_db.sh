mysqldump --user=root --host=127.0.0.1 --flush-logs --single-transaction reprompt > conf/travis.sql


  - mysql -u root reprompt < conf/travis.sql