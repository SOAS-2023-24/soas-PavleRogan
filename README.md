# Aplikacija za razmenu običnih (fiat) i crypto valuta

Ova aplikacija omogućava razmenu fiat i kripto valuta koristeći mikroservisnu arhitekturu. Aplikacija je implementirana koristeći Java programski jezik, Maven za upravljanje zavisnostima, Docker za kontejnerizaciju, i H2 kao in-memory bazu podataka.

## USERS SERVICE

### Get All Users

- **URL:** `http://localhost:8765/users`
- **Method:** `GET`

### Create a New User

- **URL:** `http://localhost:8765/users/newUser`
- **Method:** `POST`
- **Headers:** `Authorization` required.

### Update a User

- **URL:** `http://localhost:8765/users/{id}`
- **Method:** `PUT`
- **Headers:** `Authorization` required.

### Delete a User

- **URL:** `http://localhost:8765/users/{id}`
- **Method:** `DELETE`
- **Headers:** `Authorization` required.

### Get Current User's Role

- **URL:** `http://localhost:8765/users/current-user-role`
- **Method:** `GET`
- **Headers:** `Authorization` required.

### Get Current User's Email

- **URL:** `http://localhost:8765/users/current-user-email`
- **Method:** `GET`
- **Headers:** `Authorization` required.

### Get User by Email

- **URL:** `http://localhost:8765/users/by-email/{email}`
- **Method:** `GET`
