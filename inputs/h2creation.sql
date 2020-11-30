DROP TABLE IF EXISTS lab7_reservations;
DROP TABLE IF EXISTS lab7_rooms;

CREATE TABLE lab7_rooms (RoomCode CHAR(5) PRIMARY KEY, RoomName VARCHAR(30), Beds INTEGER, bedType VARCHAR(8), maxOcc INTEGER, basePrice FLOAT, decor VARCHAR(20), UNIQUE(RoomName));
CREATE TABLE lab7_reservations (CODE INTEGER PRIMARY KEY, Room CHAR(5), CheckIn DATE, Checkout DATE, Rate FLOAT, LastName VARCHAR(15), FirstName VARCHAR(15), Adults INTEGER, Kids INTEGER, FOREIGN KEY (Room) REFERENCES lab7_rooms (RoomCode));