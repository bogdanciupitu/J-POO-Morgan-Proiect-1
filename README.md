# Project

        This  project is a simplified banking system. This system handles users, accounts, cards,
    transactions, card operations like split and more.
        The design patterns which I used are Singleton for the Bank Class and Factory for the 2 types
    of accounts.
    I will add other design patterns in the second stage, like command for the Bank class.

## Classes Description

### Bank
This class uses the Singleton design pattern because we want to have only one bank in the system.
It contains methods for the commands like "addAccount", "createCard", "sendMoney" and so on.

### User
This class represents a user of a bank and it contains information like first name, last name,
email, accounts, aliases.

### Account
This class represents an user's bank account and it contains information like balance, currency, 
account type, iban, timestamp, minimum balance, transactions, cards.

#### ClassicAccount
This class extends the Account class and it represents a classic account.

#### SavingsAccount
This class extends the Account class and it represents a savings account with a interest rate.

#### AccountFactory
This class is a factory for creating instances of 'ClassicAccount' and 'SavingsAccount'.

### Card
This class represents a card and it contains information like card number and status.

#### OneTimeCard
This class extends the Card class and it represents a one-time card.

### Exchange
This class represents an exchange rate between two currencies.

### Transaction
This class represents the transactions made by a user, like send money, receive money,
split money and so on.

### Alias
This class represents an alias for a user.
