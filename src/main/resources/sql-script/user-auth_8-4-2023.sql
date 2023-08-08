CREATE TABLE user_info (
		id 			SERIAL 			PRIMARY KEY,
		name 		VARCHAR 		NOT NULL,
		created DATE 				NOT NULL,
		updated DATE 				NOT NULL,
		status 	VARCHAR(50) NOT NULL
);

CREATE TABLE user_auth (
		id 				SERIAL 				PRIMARY KEY,
		user_id 	INT 					REFERENCES user_info(id),
		email 		VARCHAR(255) 	NOT NULL,
		password 	VARCHAR(255) 	NOT NULL
);

