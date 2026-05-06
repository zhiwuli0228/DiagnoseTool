## ADDED Requirements

### Requirement: Java copyright header
The system SHALL ensure Java source files include the standard project copyright header before the package declaration.

#### Scenario: Add missing copyright header
- **WHEN** a Java source file under the maintained source tree does not include the project copyright header
- **THEN** implementation MUST add the standard copyright header before the package declaration

#### Scenario: Preserve existing valid header
- **WHEN** a Java source file already contains a valid project copyright header
- **THEN** implementation MUST preserve it and avoid duplicate headers

### Requirement: Public Java type Javadoc
The system SHALL document public Java classes, interfaces, enums, and records with meaningful Javadoc.

#### Scenario: Public type has Javadoc
- **WHEN** a Java source file declares a public type
- **THEN** that public type MUST have Javadoc immediately before the declaration

#### Scenario: Public type metadata is included
- **WHEN** a public type is documented
- **THEN** the Javadoc MUST include a meaningful summary and project-required metadata such as author and since when applicable

### Requirement: Public Java method Javadoc
The system SHALL document public methods and constructors with valid and complete Javadoc where they are part of the public source surface.

#### Scenario: Public method has useful Javadoc
- **WHEN** a Java source file declares a public method or constructor
- **THEN** the declaration MUST have Javadoc immediately before it unless it is an obvious framework override whose inherited documentation is intentionally referenced

#### Scenario: Parameters are documented
- **WHEN** a documented public method has parameters
- **THEN** every `@param` tag MUST include the parameter name and a non-empty meaningful description

#### Scenario: Return value is documented
- **WHEN** a documented public method returns a value other than `void`
- **THEN** the Javadoc MUST include a `@return` tag with a non-empty meaningful description

#### Scenario: Thrown exceptions are documented
- **WHEN** a documented public method declares checked or domain-significant exceptions
- **THEN** each documented `@throws` tag MUST include a non-empty meaningful description

#### Scenario: Invalid tags are not used
- **WHEN** Java source Javadoc is added or updated
- **THEN** it MUST NOT include invalid Javadoc tags such as `@invalidTag`

### Requirement: Java style verification
The system SHALL provide a repeatable way to verify Java copyright and public Javadoc requirements.

#### Scenario: Verification detects missing documentation
- **WHEN** maintained Java source files miss required copyright headers or public Javadocs
- **THEN** the verification step MUST fail or report the specific files that need remediation

#### Scenario: Verification passes after remediation
- **WHEN** all maintained Java source files satisfy the copyright and public Javadoc rules
- **THEN** the verification step MUST pass together with the existing Java test/build workflow
