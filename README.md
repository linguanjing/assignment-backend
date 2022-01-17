# Project Details

### Introduction
This is a SpringBoot project, to provide the REST API to be consumed by the front-end projects.

springBoot-backend (server) - REST API Provider
- Spring Data JPA
- H2 Database

react-frontend (client) - REST API Consumer
- axios http library 
- Component 

### Run 

    project_folder/mvnw spring-boot:run

### API:
#### Client Login
    /api/v1/client/login

    POST:
        {
            "loginId" : "sky",
            "password" : "password",
            "displayName": ""
        } 

#### Create New Client Account
    /api/v1/client/signup

    POST:
        {
            "loginId" : "sky",
            "password" : "password",
            "displayName": ""
        }

#### Get list of money receiver (without the login user) 
    /api/v1/client/{loginClientId}/receivers

    GET:
        loginClientId from GUI

#### New Transaction
    /api/v1/transaction/new

    POST:
        {
            "senderId" : "sky",
            "receiverId" : "bob",
            "amount": 100
        }
    
    Top-up: senderId = receiverId

#### Client Transaction Status Message
    /api/v1/transaction/{loginClientId}

    GET:
        loginClientId from GUI
    
    Return list of message
    {
        "Hello, Sky",
        "Your balance is 100",
        "Owing 20 from Alice."
    }

    Details refer to:
        ClientTransactionController.getTransationMessages
        

