# Data Access Layer Overview

The Data Access Layer manages how the CHMS retrieves and processes data from the signal generator. It is designed for flexibility and separation of concerns.

## Components

- **DataListener Interface**: Defines methods to start and stop data listening. This abstraction allows easy addition of new data sources.
- **Implementations of DataListener**:
  - `TCPDataListener`: Receives data via TCP using a port number.
  - `WebSocketDataListener`: Receives data via WebSocket using a port number.
  - `FileDataListener`: Reads data from files using a file path.
- **DataParser**: Converts raw data (e.g., strings) into `PatientData` objects usable by the system.
- **DataSourceAdapter**: Forwards parsed `PatientData` to the storage system.

## Design Principles

- **Extensibility**: New data sources can be added by implementing the `DataListener` interface.
- **Single Responsibility**: Each class has a focused role—listening, parsing, or forwarding data.
- **Clear Data Flow**: Listeners interact only with `DataParser` and `DataSourceAdapter`, ensuring a straightforward and maintainable process.

## Relationships

- Listener classes implement the `DataListener` interface.
- Listeners use `DataParser` to convert incoming data.
- Parsed data is sent to storage via `DataSourceAdapter`.

This structure decouples data acquisition from processing and storage, making the system easier to maintain and extend.