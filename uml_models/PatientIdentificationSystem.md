# Patient Identification System

This diagram illustrates how the CHMS accurately links data to the correct patient.

- **PatientIdentifier**: Accepts a patient ID and requests the corresponding record from the `IdentityManager`.
- **PatientRecord**: Stores patient details such as ID, name, and medical history, sourced from the hospital database.
- **IdentityManager**: Maintains a map of all patient records, manages adding new records, and handles mismatches (e.g., when an ID does not match any record). It logs mismatches and is the sole class responsible for modifying records.

**Design Highlights:**
- `PatientIdentifier` interacts only with `IdentityManager` to retrieve data, promoting separation of concerns.
- `IdentityManager` manages multiple `PatientRecord` objects and centralizes record control.
- Each class has a single responsibility: matching IDs, storing data, or overseeing records.
- Access to modify records is restricted to `IdentityManager`, ensuring data security.
- The design is flexible for future enhancements, such as new mismatch handling strategies.

This structure ensures reliable patient-data matching and robust error handling within the CHMS.