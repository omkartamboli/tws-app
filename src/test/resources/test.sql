SELECT * FROM tradingApp.order_entity;

SELECT * FROM tradingApp.contract_entity;

SELECT * FROM tradingApp.system_config_entity;

UPDATE `tradingApp`.`system_config_entity` SET `boolean_value` = 1 WHERE (`property` = 'divergence.order.enabled'); SELECT * FROM tradingApp.system_config_entity;

UPDATE `tradingApp`.`system_config_entity` SET `boolean_value` = 0 WHERE (`property` = 'divergence.order.enabled'); SELECT * FROM tradingApp.system_config_entity;



delete from tradingApp.order_entity where parent_order_order_id > 0  and order_id > 1;

delete from tradingApp.order_entity where order_id > 0 and order_id > 1;


delete from tradingApp.order_entity where parent_order_order_id > 0  and order_trigger is null;

delete from tradingApp.order_entity where parent_order_order_id = 206;

delete from tradingApp.order_entity where order_id > 0 and order_trigger is null;


delete from tradingApp.order_entity where order_status='PreSubmitted'; 
delete from tradingApp.order_entity where order_status='Submitted';


-- DELETE all ORDERs
-- delete from tradingApp.order_entity where parent_order_order_id > 0 ;
-- delete from tradingApp.order_entity where parent_oca_order_order_id > 0 ;
-- delete from tradingApp.order_entity;


SELECT * FROM tradingApp.order_entity; 

-- delete from tradingApp.contract_entity where ticker_id > 2;

-- drop table tradingApp.contract_entity;

-- drop table tradingApp.order_entity;

-- INSERT INTO `tradingApp`.`system_config_entity` (`property`, `value`) VALUES ('', '');

use tradingApp;


-- INSERT INTO `tradingApp`.`system_config_entity` (`property`, `value`) VALUES ('default.order.value', '50000');
-- INSERT INTO `tradingApp`.`system_config_entity` (`property`, `value`) VALUES ('target.profit.percentage', '0.5');
-- INSERT INTO `tradingApp`.`system_config_entity` (`property`, `value`) VALUES ('stoploss.percentage', '0.25');
-- INSERT INTO `tradingApp`.`system_config_entity` (`property`, `value`) VALUES ('order.start.time', '14:50');
-- INSERT INTO `tradingApp`.`system_config_entity` (`property`, `value`) VALUES ('order.end.time', '20:50');
-- INSERT INTO `tradingApp`.`system_config_entity` (`property`, `value`) VALUES ('tws.trading.account', 'DU1234567');
-- INSERT INTO `tradingApp`.`system_config_entity` (`property`, `value`) VALUES ('order.type', 'TAKE_PROFIT_ORDER');
-- INSERT INTO `tradingApp`.`system_config_entity` (`property`, `value`) VALUES ('order.type', 'TAKE_PROFIT_STOP_LOSS_ORDER');
-- INSERT INTO `tradingApp`.`system_config_entity` (`property`, `value`) VALUES ('order.type', 'TRAILING_STOP_LOSS_ORDER');

-- select * from `tradingApp`.`system_config_entity` where value = 'TRAILING_STOP_LOSS_ORDER'

delete from tradingApp.contract_entity where ticker_id > 2;



UPDATE `tradingApp`.`contract_entity` SET `active` = 1  -- WHERE (`symbol` = 'TSLA');


-- ALTER TABLE  tradingApp.contract_entity ALTER active SET DEFAULT 0;


INSERT INTO `tradingApp`.`contract_entity` (`symbol`,`currency`,`default_quantity`,`exchange`,`ltp`,`ltp_timestamp`,`sec_type`,`ticker_id`,`ask_timestamp`,`bid_timestamp`,`last_ask`,`last_bid`,`step1`,`step2`,`step3`,`step4`,`step5`,`active`,`next_fut_date`) VALUES ('MNQ','USD',2,'SMART',12208,'2023-03-09 16:19:17.789000','FUT',22,'2021-10-19 13:58:15.314000','2021-10-19 13:58:15.314000',0,0,2,5,10,20,50,1,'202306');
INSERT INTO `tradingApp`.`contract_entity` (`symbol`,`currency`,`default_quantity`,`exchange`,`ltp`,`ltp_timestamp`,`sec_type`,`ticker_id`,`ask_timestamp`,`bid_timestamp`,`last_ask`,`last_bid`,`step1`,`step2`,`step3`,`step4`,`step5`,`active`,`next_fut_date`) VALUES ('MES','USD',5,'SMART',3895,'2023-03-09 16:19:17.789000','FUT',23,'2021-10-19 13:58:15.314000','2021-10-19 13:58:15.314000',0,0,1,2,5,10,15,1,'202306');

