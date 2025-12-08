# Clearing Service API

## Rate Limits

The server will rate-limit requests above a certain rate. Please keep requests
to less than five requests per second.

## Open access endpoints

### `/`
```
route: /
methods: GET
```

This endpoint returns a hosted page that can be used to call the other APIs for
debugging.

### `/create_api_key`
```
route: /create_api_key
methods: POST
expected body: none
```

This endpoint returns (in plain text) an API key which can be used on
API-key-protected endpoints. API keys are valid for 45 minutes, after which they
will be purged from the server.

## Endpoints requiring authorization

All endpoints in this section expect an API key (returned by `/create_api_key`) to be set as a bearer token in the authorization header, e.g.:
```
Authorization: Bearer API_KEY_VALUE
```

### `/reset`
```
route: /reset
methods: POST
expected body: none
```

`POST` to this endpoint to reset server state (i.e. delete all submitted clearing
files, bank account transactions, disputes, etc).

### `/submit_clearing_file`
```
route: /submit_clearing_file
methods: POST
expected body: application/x-www-form-urlencoded form of the following shape:
  clearing_file_contents: clearing file contents, as text, in the Clearing File Format. required.
  allow_disputes: the string "true" or "false". optional.
```

`POST` to this endpoint to submit a clearing file, which the clearing service will
then process (and potentially update the bank account transactions list and the
disputes list).

### `/get_bank_account_transactions`
```
route: /get_bank_account_transactions
methods: GET
```

`GET` from this endpoint to get a list of bank account transactions

### `/validate_reconciliation_report`
```
route: /validate_reconciliation_report
methods: POST, GET
expected body: if `POST`, application/x-www-form-urlencoded form of the following shape:
  reconciliation_report_contents: reconciliation report contents, as text, in
  the Reconciliation Report Format. required.
```

`POST` to this endpoint with a reconciliation report in the reconciliation_report_contents field. The response will indicate correct and incorrect reconciliation values.

`GET` from this endpoint to get the server’s reconciliation report - could be useful for debugging.

### `/get_disputes`
```
route: /get_disputes
methods: GET
```

`GET` from this endpoint to get a list of disputes.

## File Formats

### Clearing File Format

The Clearing File Format is a fixed-width column format, with columns separated
by commas (`,`) and rows separated by newlines (`\n`).

The columns are defined as follows:

* Column 1: `ARN`. 22 bytes [`0-22`]; each byte is in the ASCII range `'0'-'9'`. This is an "acquirer reference number", and can be any unique value that identifies a given payment intent for clearing. It is allocated by the client.

* Column 2: `timestamp`. 20 bytes [`23-43`]; each byte is in the ASCII range `'0'-'9'`. This is the time that the payment intent occurred, in milliseconds since 00:00 on January 1, 1970 (standard Unix epoch).

* Column 3: `amount`. 10 bytes [`44-54`]; each byte is in the ASCII range `'0'-'9'`. This is the amount of the payment intent, in minor units (i.e. cents for USD).

* Column 4: `currency`. 3 bytes [`55-58`]; this should be set to the three-letter ISO 4217 currency code in lowercase for the payment (e.g. `usd`).


### Bank Account Transactions Format

The Bank Account Transactions format is a fixed-width column format, with columns
separated by commas (`,`) and rows separated by newlines (`\n`).

The columns are defined as follows:

* Column 1: `bank_account_transaction_id`. 8 bytes [`0-8`]; each byte is in the ASCII range `'0'-'9'; 'A'-'F'`. This is a unique ID for the bank account transaction, allocated by the server.

* Column 2: `timestamp`. 20 bytes [`9-29`]; each byte is in the ASCII range `'0'-'9'`. This is the time that the bank account transaction was posted at, in milliseconds since 00:00 on January 1, 1970 (standard Unix epoch). It is greater than the timestamps of the clearing files it is settling.

* Column 3: `amount`. 10 bytes [`30-40`]; each byte is in the ASCII range `'0'-'9'`. This is the amount of the bank account transaction, in minor units (i.e. cents for USD).

* Column 4: `currency`. 3 bytes [`41-44`]; this is set to the three-letter ISO 4217 currency code in lowercase for the bank account transaction (e.g. `usd`).

### Reconciliation Report Format

The Reconciliation Report Format is a fixed-width column format, with columns
separated by commas (`,`) and rows separated by newlines (`\n`).

The columns are defined as follows:

* Column 1: `ARN`. 22 bytes [`0-22`]; each byte is in the ASCII range `'0'-'9'`. This is the ARN of the payment intent whose status we are reporting.

* Column 2: `status`. 7 bytes [`23-30`]; this should be set to one of `PENDING`, `SETTLED`, or `DISPUTE`.

* Column 3: `bank_account_transaction_id`. 8 bytes [`0-8`]; each byte is in the ASCII range `'0'-'9'; 'A'-'F'`. If settled or disputed, this should be set to the bank account transaction ID for the bank account transaction that the payment intent was originally settled in. If the payment intent has not yet been settled, this should be set to all zeroes.


### Disputes Format

The Disputes Format is a fixed-width column format, with columns
separated by commas (`,`) and rows separated by newlines (`\n`).

The columns are defined as follows:

* Column 1: `bank_account_transaction_id`. 8 bytes [`0-8`]; each byte is in the ASCII range `'0'-'9'; 'A'-'F'`. This will be set to the value of the bank account transaction that the dispute was deducted from.

* Column 2: `ARN`. 22 bytes [`9-31`]; each byte is in the ASCII range `'0'-'9'`. This is the ARN of the payment intent which was disputed.

* Column 3: `amount`. 10 bytes [`32-42`]; each byte is in the ASCII range `'0'-'9'`. This is the amount of the dispute, in minor units (i.e. cents for USD). Payments are always fully disputed or not disputed.

* Column 4: `currency`. 3 bytes [`43-46`]; this is set to the three-letter ISO 4217 currency code in lowercase for the dispute (e.g. `usd`).