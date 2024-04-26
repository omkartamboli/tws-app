SELECT * FROM tradingApp.order_entity where created_timestamp > '2023-04-04 08:05:55.825000' order by created_timestamp desc;

SELECT * FROM tradingApp.order_entity;



SELECT * FROM tradingApp.contract_entity;

SELECT * FROM tradingApp.system_config_entity;

select * from  tradingApp.order_entity where order_id not in (1,4,7,10,37,40,43,49,52,55,58,61,64,67,70,73,76,79,82,85,88,115,118,121,124,127,130,133,136,139,142,151,154,157,160,163,166,169,172,175,178,181,184,187,190,193,196,199,202,205,208,211,214,217,220,223,226,229,232,235,238,241,244,247,250,253,256,259,261,264,267,270,273,276,279,282,285,288,297,300,303,306,309,310,313,316,319,322,325,328,337,340,343,346,349,352,355,358,361,364,367,370,376,394,398,400,403,406,409,412,415,418,421,424,427,430,433);

select distinct(parent_order_order_id) from tradingApp.order_entity;


-- UPDATE `tradingApp`.`system_config_entity` SET `boolean_value` = 1 WHERE (`property` = 'divergence.order.enabled'); SELECT * FROM tradingApp.system_config_entity;

-- UPDATE `tradingApp`.`system_config_entity` SET `boolean_value` = 0 WHERE (`property` = 'divergence.order.enabled'); SELECT * FROM tradingApp.system_config_entity;



delete from tradingApp.order_entity where parent_order_order_id > 0  and order_id > 0;

delete from tradingApp.order_entity where order_id > 0;


-- delete from tradingApp.order_entity where parent_order_order_id > 0  and order_trigger is null;

-- delete from tradingApp.order_entity where parent_order_order_id = 206;

-- delete from tradingApp.order_entity where order_id > 0 and order_trigger is null;


-- delete from tradingApp.order_entity where order_status='PreSubmitted'; 
-- delete from tradingApp.order_entity where order_status='Submitted';



/*
-- DELETE all ORDERs
delete from tradingApp.order_entity where order_id not in (1,4,7,10,37,40,43,49,52,55,58,61,64,67,70,73,76,79,82,85,88,115,118,121,124,127,130,133,136,139,142,151,154,157,160,163,166,169,172,175,178,181,184,187,190,193,196,199,202,205,208,211,214,217,220,223,226,229,232,235,238,241,244,247,250,253,256,259,261,264,267,270,273,276,279,282,285,288,297,300,303,306,309,310,313,316,319,322,325,328,337,340,343,346,349,352,355,358,361,364,367,370,376,394,398,400,403,406,409,412,415,418,421,424,427,430,433);
delete from tradingApp.order_entity where parent_order_order_id > 0 ;
delete from tradingApp.order_entity where parent_oca_order_order_id > 0 ;
delete from tradingApp.order_entity;

*/



SELECT * FROM tradingApp.contract_entity;

SELECT * from tradingApp.system_config_entity;


update tradingApp.contract_entity
set default_stop_loss = null ;

-- select * from  tradingApp.order_entity;

-- select order_id,sequence_id,quantity,filled,order_type from  tradingApp.order_entity where order_id >= 217 and sequence_id is not null order by order_id;

select order_id,sequence_id,quantity,filled,order_type,transaction_price,avg_fill_price,ots_order_type from  tradingApp.order_entity where sequence_id like '%240409%' ;

select order_id,sequence_id,quantity,filled,order_type,ots_order_type from  tradingApp.order_entity where (filled is null or filled < quantity) order by symbol,order_id;



-- ---select * from  tradingApp.order_entity as a join tradingApp.order_entity as b on a.sequence_id=b.sequence_id and a.order_id <> b.order_id where (b.filled is null or b.filled < b.quantity) and a.ots_order_type <> b.ots_order_type;

select order_id,sequence_id,quantity,filled,order_type,ots_order_type from  tradingApp.order_entity where (ots_order_type = 'SX' or ots_order_type = 'LX') and (filled is null or filled < quantity);


select order_id,sequence_id,quantity,filled,order_type,ots_order_type from  tradingApp.order_entity where (filled is null or filled < quantity);

select order_id,sequence_id,quantity,filled,order_type,ots_order_type from  tradingApp.order_entity where sequence_id like '%2404042112%' or sequence_id like '%2404050135%';


use tradingApp;

-- verify open positions
select order_id,sequence_id,quantity,filled,order_type,ots_order_type from  tradingApp.order_entity where sequence_id in (select id from (select sequence_id as id , count(sequence_id) as count_si from  tradingApp.order_entity group by sequence_id) as table_a where count_si < 2);



-- All orders
select order_id,sequence_id,quantity,filled,order_type,ots_order_type from  tradingApp.order_entity where sequence_id is not null order by order_id;

-- incomplete orders
select order_id,sequence_id,transaction_price,quantity,filled,order_type,ots_order_type from  tradingApp.order_entity where sequence_id is not null and (filled is null or filled < quantity) order by order_id;
-- select * from  tradingApp.order_entity where sequence_id is not null and (filled is null or filled < quantity) order by order_id;








-- update  tradingApp.contract_entity set default_stop_loss=10 where  default_stop_loss is null;

-- update  tradingApp.contract_entity set oca_Hedge_Multiplier=1;

-- delete from tradingApp.contract_entity;

-- drop table tradingApp.contract_entity;

-- drop table tradingApp.order_entity;

-- INSERT INTO `tradingApp`.`system_config_entity` (`property`, `value`) VALUES ('', '');

-- INSERT INTO `tradingApp`.`system_config_entity` (`property`, `value`) VALUES ('default.order.value', '50000');
-- INSERT INTO `tradingApp`.`system_config_entity` (`property`, `value`) VALUES ('target.profit.percentage', '0.5');
-- INSERT INTO `tradingApp`.`system_config_entity` (`property`, `value`) VALUES ('stoploss.percentage', '0.25');
-- INSERT INTO `tradingApp`.`system_config_entity` (`property`, `value`) VALUES ('order.start.time', '14:50');
-- INSERT INTO `tradingApp`.`system_config_entity` (`property`, `value`) VALUES ('order.end.time', '20:50');
-- INSERT INTO `tradingApp`.`system_config_entity` (`property`, `value`) VALUES ('tws.trading.account', 'DU1234567');
-- INSERT INTO `tradingApp`.`system_config_entity` (`property`, `value`) VALUES ('order.type', 'TAKE_PROFIT_ORDER');
-- INSERT INTO `tradingApp`.`system_config_entity` (`property`, `value`) VALUES ('order.type', 'TAKE_PROFIT_STOP_LOSS_ORDER');
-- INSERT INTO `tradingApp`.`system_config_entity` (`property`, `value`) VALUES ('order.type', 'TRAILING_STOP_LOSS_ORDER');

INSERT INTO `tradingApp`.`system_config_entity` (`property`, `boolean_value`) VALUES ('order.pessimisticOrderExitPrice', 1);


-- select * from `tradingApp`.`system_config_entity`;




-- select * from `tradingApp`.`system_config_entity` where value = 'TRAILING_STOP_LOSS_ORDER'

-- delete from tradingApp.contract_entity where ticker_id > 0;



-- UPDATE `tradingApp`.`contract_entity` SET `active` = 1  -- WHERE (`symbol` = 'TSLA');


-- ALTER TABLE  tradingApp.contract_entity ALTER active SET DEFAULT 0;


-- INSERT INTO `tradingApp`.`contract_entity` (`symbol`,`currency`,`default_quantity`,`exchange`,`ltp`,`ltp_timestamp`,`sec_type`,`ticker_id`,`ask_timestamp`,`bid_timestamp`,`last_ask`,`last_bid`,`step1`,`step2`,`step3`,`step4`,`step5`,`active`,`next_fut_date`) VALUES ('MNQ','USD',2,'SMART',12208,'2023-03-09 16:19:17.789000','FUT',22,'2021-10-19 13:58:15.314000','2021-10-19 13:58:15.314000',0,0,2,5,10,20,50,1,'202306');
-- INSERT INTO `tradingApp`.`contract_entity` (`symbol`,`currency`,`default_quantity`,`exchange`,`ltp`,`ltp_timestamp`,`sec_type`,`ticker_id`,`ask_timestamp`,`bid_timestamp`,`last_ask`,`last_bid`,`step1`,`step2`,`step3`,`step4`,`step5`,`active`,`next_fut_date`) VALUES ('MES','USD',5,'SMART',3895,'2023-03-09 16:19:17.789000','FUT',23,'2021-10-19 13:58:15.314000','2021-10-19 13:58:15.314000',0,0,1,2,5,10,15,1,'202306');


/*

use tradingApp;

INSERT INTO `contract_entity` (`symbol`,`currency`,`default_quantity`,`exchange`,`ltp`,`ltp_timestamp`,`sec_type`,`ticker_id`,`ask_timestamp`,`bid_timestamp`,`last_ask`,`last_bid`,`step1`,`step2`,`step3`,`step4`,`step5`,`active`,`next_fut_date`,`default_stop_loss`,`oca_Hedge_Multiplier`, `use_oca_hedge_order`) VALUES ('AAPL','USD',100,'SMART',154.7,'2023-03-18 01:20:18.208000','STK',1,'2021-06-08 16:21:19.254000','2021-06-08 16:21:19.247000',126.59,126.58,0.03,0.06,0.15,0.25,0.5,1,NULL,2,1,0);
INSERT INTO `contract_entity` (`symbol`,`currency`,`default_quantity`,`exchange`,`ltp`,`ltp_timestamp`,`sec_type`,`ticker_id`,`ask_timestamp`,`bid_timestamp`,`last_ask`,`last_bid`,`step1`,`step2`,`step3`,`step4`,`step5`,`active`,`next_fut_date`,`default_stop_loss`,`oca_Hedge_Multiplier`, `use_oca_hedge_order`) VALUES ('ABNB','USD',100,'SMART',118.5,'2023-03-18 02:04:44.912000','STK',18,'2021-10-19 13:59:28.832000','2021-10-19 13:59:28.832000',0,0,0.1,0.2,0.5,1,2,0,NULL,1.5,1,0);
INSERT INTO `contract_entity` (`symbol`,`currency`,`default_quantity`,`exchange`,`ltp`,`ltp_timestamp`,`sec_type`,`ticker_id`,`ask_timestamp`,`bid_timestamp`,`last_ask`,`last_bid`,`step1`,`step2`,`step3`,`step4`,`step5`,`active`,`next_fut_date`,`default_stop_loss`,`oca_Hedge_Multiplier`, `use_oca_hedge_order`) VALUES ('AMD','USD',100,'SMART',97.6,'2023-03-17 23:59:34.635000','STK',13,'2021-10-19 13:58:53.152000','2021-10-19 13:58:53.152000',0,0,0.1,0.2,0.5,1,2,1,NULL,1,1,0);
INSERT INTO `contract_entity` (`symbol`,`currency`,`default_quantity`,`exchange`,`ltp`,`ltp_timestamp`,`sec_type`,`ticker_id`,`ask_timestamp`,`bid_timestamp`,`last_ask`,`last_bid`,`step1`,`step2`,`step3`,`step4`,`step5`,`active`,`next_fut_date`,`default_stop_loss`,`oca_Hedge_Multiplier`, `use_oca_hedge_order`) VALUES ('AMZN','USD',100,'SMART',98.9,'2023-03-18 02:15:01.635000','STK',11,'2021-10-19 13:58:34.716000','2021-10-19 13:58:34.716000',0,0,0.1,0.2,0.5,1,2,1,NULL,1,1,0);
INSERT INTO `contract_entity` (`symbol`,`currency`,`default_quantity`,`exchange`,`ltp`,`ltp_timestamp`,`sec_type`,`ticker_id`,`ask_timestamp`,`bid_timestamp`,`last_ask`,`last_bid`,`step1`,`step2`,`step3`,`step4`,`step5`,`active`,`next_fut_date`,`default_stop_loss`,`oca_Hedge_Multiplier`, `use_oca_hedge_order`) VALUES ('COIN','USD',100,'SMART',75.82,'2023-03-18 02:15:01.663000','STK',6,'2021-10-19 13:58:01.452000','2021-10-19 13:58:01.452000',0,0,0.1,0.2,0.5,1,2,1,NULL,0.75,1,0);
INSERT INTO `contract_entity` (`symbol`,`currency`,`default_quantity`,`exchange`,`ltp`,`ltp_timestamp`,`sec_type`,`ticker_id`,`ask_timestamp`,`bid_timestamp`,`last_ask`,`last_bid`,`step1`,`step2`,`step3`,`step4`,`step5`,`active`,`next_fut_date`,`default_stop_loss`,`oca_Hedge_Multiplier`, `use_oca_hedge_order`) VALUES ('ES','USD',3,'SMART',3951.5,'2023-03-17 16:26:44.675000','FUT',21,'2021-10-19 13:58:15.314000','2021-10-19 13:58:15.314000',0,0,1,2,5,10,15,1,'202306',5,1,0);
INSERT INTO `contract_entity` (`symbol`,`currency`,`default_quantity`,`exchange`,`ltp`,`ltp_timestamp`,`sec_type`,`ticker_id`,`ask_timestamp`,`bid_timestamp`,`last_ask`,`last_bid`,`step1`,`step2`,`step3`,`step4`,`step5`,`active`,`next_fut_date`,`default_stop_loss`,`oca_Hedge_Multiplier`, `use_oca_hedge_order`) VALUES ('META','USD',100,'SMART',195.78,'2022-06-07 20:58:22.085000','STK',10,'2021-10-19 13:58:28.301000','2021-10-19 13:58:28.301000',0,0,0.1,0.2,0.5,1,2,0,NULL,2,1,0);
INSERT INTO `contract_entity` (`symbol`,`currency`,`default_quantity`,`exchange`,`ltp`,`ltp_timestamp`,`sec_type`,`ticker_id`,`ask_timestamp`,`bid_timestamp`,`last_ask`,`last_bid`,`step1`,`step2`,`step3`,`step4`,`step5`,`active`,`next_fut_date`,`default_stop_loss`,`oca_Hedge_Multiplier`, `use_oca_hedge_order`) VALUES ('GOOG','USD',100,'SMART',102.12,'2023-03-18 02:15:01.669000','STK',9,'2021-10-19 13:58:20.655000','2021-10-19 13:58:20.655000',0,0,0.1,0.2,0.5,1,2,1,NULL,1,1,0);
INSERT INTO `contract_entity` (`symbol`,`currency`,`default_quantity`,`exchange`,`ltp`,`ltp_timestamp`,`sec_type`,`ticker_id`,`ask_timestamp`,`bid_timestamp`,`last_ask`,`last_bid`,`step1`,`step2`,`step3`,`step4`,`step5`,`active`,`next_fut_date`,`default_stop_loss`,`oca_Hedge_Multiplier`, `use_oca_hedge_order`) VALUES ('JPM','USD',100,'SMART',125.75,'2023-03-18 02:15:01.674000','STK',7,'2021-10-19 13:58:09.131000','2021-10-19 13:58:09.131000',0,0,0.1,0.2,0.5,1,2,0,NULL,1,1,0);
INSERT INTO `contract_entity` (`symbol`,`currency`,`default_quantity`,`exchange`,`ltp`,`ltp_timestamp`,`sec_type`,`ticker_id`,`ask_timestamp`,`bid_timestamp`,`last_ask`,`last_bid`,`step1`,`step2`,`step3`,`step4`,`step5`,`active`,`next_fut_date`,`default_stop_loss`,`oca_Hedge_Multiplier`, `use_oca_hedge_order`) VALUES ('MES','USD',30,'SMART',3951.5,'2023-03-17 16:26:44.678000','FUT',23,'2021-10-19 13:58:15.314000','2021-10-19 13:58:15.314000',0,0,1,2,5,10,15,1,'202306',5,1,0);
INSERT INTO `contract_entity` (`symbol`,`currency`,`default_quantity`,`exchange`,`ltp`,`ltp_timestamp`,`sec_type`,`ticker_id`,`ask_timestamp`,`bid_timestamp`,`last_ask`,`last_bid`,`step1`,`step2`,`step3`,`step4`,`step5`,`active`,`next_fut_date`,`default_stop_loss`,`oca_Hedge_Multiplier`, `use_oca_hedge_order`) VALUES ('MNQ','USD',10,'SMART',12630,'2023-03-17 16:26:45.976000','FUT',22,'2021-10-19 13:58:15.314000','2021-10-19 13:58:15.314000',0,0,2,5,10,20,50,1,'202306',20,1,0);
INSERT INTO `contract_entity` (`symbol`,`currency`,`default_quantity`,`exchange`,`ltp`,`ltp_timestamp`,`sec_type`,`ticker_id`,`ask_timestamp`,`bid_timestamp`,`last_ask`,`last_bid`,`step1`,`step2`,`step3`,`step4`,`step5`,`active`,`next_fut_date`,`default_stop_loss`,`oca_Hedge_Multiplier`, `use_oca_hedge_order`) VALUES ('MSFT','USD',100,'SMART',279.25,'2023-03-18 02:04:58.280000','STK',4,'2021-10-19 13:55:54.873000','2021-10-19 13:55:54.873000',0,0,0.1,0.2,0.5,1,2,1,NULL,3,1,0);
INSERT INTO `contract_entity` (`symbol`,`currency`,`default_quantity`,`exchange`,`ltp`,`ltp_timestamp`,`sec_type`,`ticker_id`,`ask_timestamp`,`bid_timestamp`,`last_ask`,`last_bid`,`step1`,`step2`,`step3`,`step4`,`step5`,`active`,`next_fut_date`,`default_stop_loss`,`oca_Hedge_Multiplier`, `use_oca_hedge_order`) VALUES ('NFLX','USD',100,'SMART',303.33,'2023-03-18 02:15:01.680000','STK',12,'2021-10-19 13:58:42.447000','2021-10-19 13:58:42.447000',0,0,0.1,0.2,0.5,1,2,1,NULL,3,1,0);
INSERT INTO `contract_entity` (`symbol`,`currency`,`default_quantity`,`exchange`,`ltp`,`ltp_timestamp`,`sec_type`,`ticker_id`,`ask_timestamp`,`bid_timestamp`,`last_ask`,`last_bid`,`step1`,`step2`,`step3`,`step4`,`step5`,`active`,`next_fut_date`,`default_stop_loss`,`oca_Hedge_Multiplier`, `use_oca_hedge_order`) VALUES ('NIO','USD',500,'SMART',8.23,'2023-03-18 02:15:01.710000','STK',3,'2021-06-08 16:21:19.752000','2021-06-08 16:21:19.492000',43.01,43,0.01,0.02,0.05,0.1,0.2,0,NULL,0.2,1,0);
INSERT INTO `contract_entity` (`symbol`,`currency`,`default_quantity`,`exchange`,`ltp`,`ltp_timestamp`,`sec_type`,`ticker_id`,`ask_timestamp`,`bid_timestamp`,`last_ask`,`last_bid`,`step1`,`step2`,`step3`,`step4`,`step5`,`active`,`next_fut_date`,`default_stop_loss`,`oca_Hedge_Multiplier`, `use_oca_hedge_order`) VALUES ('NQ','USD',1,'SMART',12629.5,'2023-03-17 16:26:45.878000','FUT',20,'2021-10-19 13:58:15.314000','2021-10-19 13:58:15.314000',0,0,2,5,10,20,50,1,'202306',20,1,0);
INSERT INTO `contract_entity` (`symbol`,`currency`,`default_quantity`,`exchange`,`ltp`,`ltp_timestamp`,`sec_type`,`ticker_id`,`ask_timestamp`,`bid_timestamp`,`last_ask`,`last_bid`,`step1`,`step2`,`step3`,`step4`,`step5`,`active`,`next_fut_date`,`default_stop_loss`,`oca_Hedge_Multiplier`, `use_oca_hedge_order`) VALUES ('NVDA','USD',100,'SMART',256.42,'2023-03-18 02:15:01.793000','STK',16,'2021-10-19 13:59:16.371000','2021-10-19 13:59:16.371000',0,0,0.1,0.2,0.5,1,2,1,NULL,2.5,1,0);
INSERT INTO `contract_entity` (`symbol`,`currency`,`default_quantity`,`exchange`,`ltp`,`ltp_timestamp`,`sec_type`,`ticker_id`,`ask_timestamp`,`bid_timestamp`,`last_ask`,`last_bid`,`step1`,`step2`,`step3`,`step4`,`step5`,`active`,`next_fut_date`,`default_stop_loss`,`oca_Hedge_Multiplier`, `use_oca_hedge_order`) VALUES ('PYPL','USD',100,'SMART',72.89,'2023-03-18 02:15:01.980000','STK',15,'2021-10-19 13:59:08.403000','2021-10-19 13:59:08.403000',0,0,0.1,0.2,0.5,1,2,0,NULL,1,1,0);
INSERT INTO `contract_entity` (`symbol`,`currency`,`default_quantity`,`exchange`,`ltp`,`ltp_timestamp`,`sec_type`,`ticker_id`,`ask_timestamp`,`bid_timestamp`,`last_ask`,`last_bid`,`step1`,`step2`,`step3`,`step4`,`step5`,`active`,`next_fut_date`,`default_stop_loss`,`oca_Hedge_Multiplier`, `use_oca_hedge_order`) VALUES ('SHOP','USD',100,'SMART',44.62,'2023-03-18 02:15:02.079000','STK',17,'2021-10-19 13:59:20.975000','2021-10-19 13:59:20.975000',0,0,0.1,0.2,0.5,1,2,0,NULL,0.5,1,0);
INSERT INTO `contract_entity` (`symbol`,`currency`,`default_quantity`,`exchange`,`ltp`,`ltp_timestamp`,`sec_type`,`ticker_id`,`ask_timestamp`,`bid_timestamp`,`last_ask`,`last_bid`,`step1`,`step2`,`step3`,`step4`,`step5`,`active`,`next_fut_date`,`default_stop_loss`,`oca_Hedge_Multiplier`, `use_oca_hedge_order`) VALUES ('TSLA','USD',100,'SMART',179.05,'2023-03-17 23:59:30.411000','STK',2,'2021-06-08 16:21:19.757000','2021-06-08 16:21:19.004000',597.81,597.51,0.5,1,2,5,10,1,NULL,1.5,1,0);
INSERT INTO `contract_entity` (`symbol`,`currency`,`default_quantity`,`exchange`,`ltp`,`ltp_timestamp`,`sec_type`,`ticker_id`,`ask_timestamp`,`bid_timestamp`,`last_ask`,`last_bid`,`step1`,`step2`,`step3`,`step4`,`step5`,`active`,`next_fut_date`,`default_stop_loss`,`oca_Hedge_Multiplier`, `use_oca_hedge_order`) VALUES ('V','USD',100,'SMART',217.4,'2023-03-18 02:15:02.380000','STK',8,'2021-10-19 13:58:15.314000','2021-10-19 13:58:15.314000',0,0,0.1,0.2,0.5,1,2,0,NULL,2,1,0);


*/
