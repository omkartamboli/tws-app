SELECT * FROM tradingApp.order_entity;

SELECT * FROM tradingApp.contract_entity;

SELECT * FROM tradingApp.system_config_entity;



delete from tradingApp.order_entity where parent_order_order_id > 0  and order_id > 1;

delete from tradingApp.order_entity where order_id > 0 and order_id > 1;


delete from tradingApp.order_entity where parent_order_order_id > 0  and order_trigger is null;

delete from tradingApp.order_entity where order_id > 0 and order_trigger is null;


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
