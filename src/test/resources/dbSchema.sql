create database tradingApp;
create user 'tradingApp'@'localhost' identified by 'tradingApp';
grant all privileges on tradingApp.* to 'tradingApp'@'localhost' with grant option;


