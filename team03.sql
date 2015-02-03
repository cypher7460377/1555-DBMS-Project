--CS1555 Term Project
--Matthew  -- MAO73@pitt.edu
--Ping Lang -- pil8@pitt.edu

-- 1. Create the tables and insert necessary data

drop table flight cascade constraints;
drop table plane cascade constraints;
drop table price cascade constraints;
drop table customer cascade constraints;
drop table reservation cascade constraints;
drop table reservation_detail cascade constraints;
drop table our_sys_time cascade constraints;
DROP SEQUENCE customer_id_sequence;


create table plane (
	plane_type char(4),
	manufacture varchar(10),
	plane_capacity int,
	last_service date,
	year int,
	constraint pk_plane primary key (plane_type) deferrable
);

create table flight (
	flight_number varchar(3),
	plane_type char(4),
	departure_city varchar(3),
	arrival_city varchar(3),
	departure_time varchar(4),
	arrival_time varchar(4),
	weekly_schedule varchar(7),
	constraint pk_flight primary key (flight_number)deferrable,
	constraint fk_plane_type foreign key (plane_type) references plane(plane_type)on delete cascade initially deferred deferrable
);

create table price (
	departure_city varchar(3),
	arrival_city varchar(3),
	high_price int,
	low_price int,
	constraint pk_price primary key (departure_city, arrival_city)deferrable
);

create table customer(
	cid					varchar(9),
	salutation			varchar(3),
	first_name			varchar(30),
	last_name			varchar(30),
	credit_card_num		varchar(16),
	credit_card_expire	date,
	street				varchar(30),
	state				varchar(2),
	city				varchar(30),
	phone				varchar(10),
	email				varchar(30),
	constraint pk_customer primary key(cid)deferrable
	);
	
create table reservation(
	reservation_number	varchar(5),
	cid					varchar(9),
	cost				int,
	reservation_date	date,
	ticketed			varchar(1),
	constraint pk_reservation primary key(reservation_number)deferrable,
	constraint fk_customer_reservation foreign key (cid) references customer(cid) on delete cascade initially deferred deferrable
	);
	
create table reservation_detail(
	reservation_number	varchar(5),
	flight_number		varchar(3),
	flight_date			date,
	leg					int,
	constraint pk_res_det primary key(reservation_number,leg)deferrable,
	constraint fk_res_det_reservation foreign key(reservation_number) references reservation(reservation_number) on delete cascade initially deferred deferrable,
	constraint fk_res_det_flight foreign key(flight_number) references flight(flight_number)on delete cascade initially deferred deferrable
	);
	
create table our_sys_time(
	c_date date,
	constraint pk_our_date primary key(c_date)deferrable
);

alter session set nls_date_format = 'MM/DD/YYYY HH24:MI:SS';

---- insert sample data
insert into plane values('B737', 'Boeing', 125, to_date('09/09/2009', 'MM/DD/YYYY'), 1996);
insert into plane values('A320', 'Airbus', 1, to_date('10/01/2011', 'MM/DD/YYYY'), 2001);
insert into plane values('E145', 'Embraer', 1, to_date('06/15/2010', 'MM/DD/YYYY'), 2008);

insert into flight values('153', 'A320', 'PIT', 'JFK', '1000', '1120', 'SMTWTFS');
insert into flight values('154', 'B737', 'JFK', 'DCA', '1230', '1320', 'S-TW-FS');
insert into flight values('552', 'E145', 'PIT', 'DCA', '1100', '1150', 'SM-WT-S');

insert into price values('PIT', 'JFK', 250, 120);
insert into price values('JFK', 'PIT', 250, 120);
insert into price values('JFK', 'DCA', 220, 100);
insert into price values('DCA', 'JFK', 210, 90);
insert into price values('PIT', 'DCA', 200, 150);
insert into price values('DCA', 'PIT', 200, 150);

-- I made up to test the triggers

/* insert into customer values('01', 'Mr', 'Peter', 'Lin','0000000000000000', to_date('09/09/2017', 'MM/DD/YYYY'), 'street','pa','city','phone','email');
insert into customer values('02', 'Ms', 'Mary', 'Lin','0000000000000000', to_date('09/09/2017', 'MM/DD/YYYY'), 'street','pa','city','phone','email');

insert into reservation values('10001', '01', 400, to_date('09/01/2013', 'MM/DD/YYYY'), 'Y');
insert into reservation values('10002', '02', 400, to_date('09/01/2013', 'MM/DD/YYYY'), 'N');

insert into reservation_detail values('10001', '153', to_date('09/09/2013', 'MM/DD/YYYY'), 1);
insert into reservation_detail values('10002', '153', to_date('09/09/2013', 'MM/DD/YYYY'), 2);
insert into reservation_detail values('10001', '154', to_date('09/09/2013', 'MM/DD/YYYY'), 2);
insert into reservation_detail values('10002', '552', to_date('09/09/2013', 'MM/DD/YYYY'), 1);

insert into our_sys_time values(to_date('09/07/2013 23:25:00', 'MM/DD/YYYY HH24:MI:SS')); */

commit;

--triggers
--#1 AdjustTicket

CREATE OR REPLACE PROCEDURE newprice(
	cost_diff	in	number, 
	flight_no	in	number, 
	hi_lo		in	number)

IS

BEGIN
	if (hi_lo = 0) then
		update reservation
		set cost = cost - cost_diff
		where reservation_number in ( 
			select reservation_number
			from 
			--Gets the reservations and flight numbers that are round trips on different days
			(select reservation_number, flight_number from reservation_detail where flight_number = flight_no)
			natural join
			(select reservation_number, count(distinct flight_date)
				from reservation_detail 
				having count(distinct flight_date)>1 
				group by reservation_number)				
		);
	Else
		update reservation
		set cost = cost - cost_diff
		where reservation_number in (
			select reservation_number
			from 
			--Gets the reservations and flight numbers that are round trips on different days
			((select reservation_number, flight_number from reservation_detail where flight_number = flight_no)
			natural join
			(select reservation_number, count(distinct flight_date)
				from reservation_detail 
				having count(distinct flight_date)=1 
				group by reservation_number))
		);
	END IF;	
END;
/

CREATE OR REPLACE TRIGGER adjustTicket
AFTER UPDATE ON price
for each row
declare
	flight_no 	flight.flight_number%TYPE;
	high_diff	integer;
	low_diff	integer;
begin
	high_diff := :old.high_price - :new.high_price;
	low_diff := :old.low_price - :new.low_price;
	select flight_number into flight_no
	from flight where departure_city = :old.departure_city and arrival_city = :old.arrival_city;
	
	if UPDATING('low_price') THEN
		newprice(low_diff, flight_no, 0);
	ELSE
		newprice(high_diff, flight_no, 1);
	end if;
end;
/


--#2 upgrade plane
--create a function to calculate current capacity of a flight
create or replace function fun_current_cap(flightNum reservation_detail.flight_number%type, flightDate reservation_detail.flight_date%type)
return number
is
	currentCapacity number;
	missing_input exception;   
begin
    begin
    if flightNum is NULL or flightDate is NULL
    then raise missing_input;
	else	
		select count(reservation_number) into currentCapacity
		from reservation_detail
		group by flight_number, flight_date
		having flight_number = flightNum  and flight_date = flightDate;
	end if;
    
    exception
    when missing_input 
	then DBMS_OUTPUT.PUT_LINE('Missing input'); 
    end;
	
    return currentCapacity;
    
end fun_current_cap;
/

-- create a function to switch a new plane type
create or replace function fun_switch_plane_type(currentCapacity int)
return plane.plane_type%type
is
   rightPlaneType plane.plane_type%type; 
   missing_input exception;
   
begin
	begin
    if currentCapacity is NULL 
    then raise missing_input;
	else	
		select plane_type into rightPlaneType
		from (select * from plane where plane_capacity >= currentCapacity order by plane_capacity asc)
		where rownum = 1
		order by rownum;
	end if;
    
    exception
    when missing_input 
    then DBMS_OUTPUT.PUT_LINE('Missing input'); 
    end;
	
    return rightPlaneType;
    
end fun_switch_plane_type;
/

-- trigger to do planeUpgrade
create or replace trigger planeUpgrade_trigger
before insert or update on reservation_detail
for each row
declare
	currentReservation integer;
	planeCapacity integer;
begin
	select plane_capacity into planeCapacity
	from plane p
	where p.plane_type = (select f.plane_type from flight f 
							where flight_number=:new.flight_number);
							
	currentReservation := fun_current_cap(:new.flight_number,:new.flight_date) + 1;	
	
	if currentReservation > planeCapacity
	then
		update flight
		set plane_type = fun_switch_plane_type(currentReservation)
		where flight_number =:new.flight_number;
	end if;
	
END planeUpgrade_trigger;
/

-- 3# trigger to do cancelReservation
-- create a function to calculate time
--create or replace function fun_time(flightNum flight_number.flight%type, ourTime c_date.our_sys_time%type)

--create a view that contains the records should be deleted
create or replace view cancel_reservation as
select *
from reservation_detail rd
where rd.reservation_number in (select reservation_number from reservation where ticketed = 'N')
and extract(day from(rd.flight_date))*24 - (select extract(day from (c_date))  from our_sys_time where rownum=1)*24 + (select extract(hour from (cast(to_date(departure_time, 'HH24:MI') as timestamp) )) from flight where flight_number = rd.flight_number) - (select extract(hour from (cast(c_date as timestamp)))from our_sys_time where rownum=1) <=12;

--delete from reservation
CREATE OR REPLACE TRIGGER cancelReservation_trigger
after insert or update on our_sys_time
declare
	flightNum reservation_detail.flight_number%type;
	flightDate reservation_detail.flight_date%type;
	currentReservation integer;
	planeCapacity integer;
	
	CURSOR delete_cur
	IS
		SELECT distinct flight_date, flight_number
		FROM cancel_reservation;
		
begin
 	delete from reservation
	where reservation_number in (select rd.reservation_number 
									from reservation_detail rd
									where extract(day from(rd.flight_date))*24 - (select extract(day from (c_date))  from our_sys_time where rownum=1)*24 + (select extract(hour from (cast(to_date(departure_time, 'HH24:MI') as timestamp) )) from flight where flight_number = rd.flight_number) - (select extract(hour from (cast(c_date as timestamp)))from our_sys_time where rownum=1) <=12)
	and ticketed = 'N'; 
	
	OPEN delete_cur;
	LOOP
		FETCH delete_cur INTO flightDate, flightNum;
		EXIT WHEN delete_cur%NOTFOUND;
		
		currentReservation := fun_current_cap(flightNum,flightDate);	
	
		select plane_capacity into planeCapacity
		from plane p
		where p.plane_type = (select f.plane_type from flight f 
							where flight_number = flightNum);
	
		if currentReservation < planeCapacity
		then
			update flight
			set plane_type = fun_switch_plane_type(currentReservation)
			where flight_number = flightNum;
		end if;	
	END LOOP;
	CLOSE delete_cur;
END;
/

CREATE SEQUENCE customer_id_sequence
  START WITH 1234
  INCREMENT BY 1
  nomaxvalue;
 
 
  CREATE OR REPLACE TRIGGER insert_new_customer
  BEFORE INSERT ON customer
  FOR EACH ROW
BEGIN
  SELECT customer_id_sequence.nextval
    INTO :new.cid
    FROM dual;
END;
/

CREATE SEQUENCE reservation_id_sequence
  START WITH 10010
  INCREMENT BY 1
  nomaxvalue;
 
 
  CREATE OR REPLACE TRIGGER insert_new_reservation
  BEFORE INSERT ON reservation
  FOR EACH ROW
BEGIN
  SELECT reservation_id_sequence.nextval
    INTO :new.reservation_number
    FROM dual;
END;
/
create or replace function check_flight_days(
	p_string1 in varchar2,
	p_string2 in varchar2)
	return	number

	as

	pos					number;
	same_day_flight		number;
begin
	pos := 1;
	same_day_flight := 0;
	
	while pos <= 7 loop
		if (substr(p_string1, pos, 1) = substr(p_string2, pos, 1)) THEN
			same_day_flight := 1;
			EXIT;
		end if;
		pos := pos +1;
	end loop;
	return same_day_flight;
end check_flight_days;
/
CREATE OR REPLACE FUNCTION format_schedule(
	some_day in varchar2
	)
	return varchar2
	
	is
	
	v_comp				varchar(9) := TRIM(some_day);
	formatted_schedule	varchar(7);
	
	BEGIN
		IF (v_comp = 'SUNDAY') THEN
			formatted_schedule := 'S------';
		ELSIF (v_comp = 'MONDAY') THEN
			formatted_schedule := '-M-----';
		ELSIF (v_comp = 'TUESDAY') THEN
			formatted_schedule := '--T----';
		ELSIF (v_comp = 'WEDNESDAY') THEN
			formatted_schedule := '---W---';
		ELSIF (v_comp = 'THURSDAY') THEN
			formatted_schedule := '----T--';
		ELSIF (v_comp = 'FRIDAY') THEN
			formatted_schedule := '-----F-';
		ELSIF (v_comp = 'SATURDAY') THEN
			formatted_schedule := '------S';
		ELSE
			dbms_output.put_line('Something went wrong!');
		END IF;
		
		RETURN formatted_schedule;
	END;
/