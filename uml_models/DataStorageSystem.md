# Data Storage System Overview

This diagram illustrates how the CHMS securely stores and retrieves patient data.

## Components

- **DataStorage (Interface):**  
  Defines methods for storing, retrieving, and deleting patient data. Enables flexibility to switch storage backends (e.g., from files to databases).

- **FileDataStorage (Class):**  
  Implements `DataStorage` to persist data as files in a specified directory.

- **PatientData (Class):**  
  Represents a single data entry (e.g., heart rate), including patient ID, timestamp, label, value, and a version number for tracking changes.

- **DataRetriever (Class):**  
  Allows medical staff to retrieve patient data by specifying patient ID and time range.

- **AccessController (Class):**  
  Checks if a staff member is authorized to access patient data, using a list of permitted users to ensure privacy and security.

## Design Principles

- **Single Responsibility:**  
  Each class has a distinct role:  
  - `FileDataStorage` handles data persistence  
  - `DataRetriever` manages data access  
  - `AccessController` enforces permissions

- **Extensibility:**  
  The `DataStorage` interface allows for future changes in storage mechanisms.

- **Security & Privacy:**  
  Access to patient data is controlled, and only authorized users can retrieve information.

- **Data Lifecycle:**  
  Old data can be deleted after a retention period (e.g., 30 days).

## Relationships

- `FileDataStorage` manages multiple `PatientData` objects.
- `DataRetriever` interacts with `DataStorage` for data retrieval, after verifying permissions with `AccessController`.

This design ensures secure storage, controlled access, and easy retrieval of patient data, while supporting future system growth.