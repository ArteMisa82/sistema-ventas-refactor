# Sistema de Ventas - Refactorización SOLID
## 1. Descripción
Este proyecto corresponde a la refactorización de un Sistema de Ventas desarrollado en Java Swing con PostgreSQL.

El objetivo principal de la refactorización es mejorar la estructura, mantenibilidad, escalabilidad y capacidad de prueba del sistema mediante

la aplicación de los principios SOLID y la identificación y reducción de antipatrones STUPID presentes en la implementación original.

## 2. Objetivos de la refactorización
- Aplicar los principios SOLID.

- Identificar y reducir antipatrones STUPID.

- Separar las responsabilidades de presentación, negocio y persistencia.

- Reducir el acoplamiento entre componentes.

- Mejorar la capacidad de prueba del sistema.

- Eliminar código duplicado cuando sea identificado.

- Utilizar nombres más descriptivos para clases, métodos y variables.

- Mantener las funcionalidades existentes del sistema original.

## 3. Estado inicial
Durante la revisión inicial se identificaron clases con múltiples responsabilidades, dependencias directas entre la interfaz gráfica y las

clases de acceso a datos, código duplicado y componentes difíciles de probar de manera aislada.

## 5. Comparación de la estructura antes y después
La refactorización modificó principalmente la organización interna del sistema, manteniendo las funcionalidades principales de la aplicación original.

### 5.1 Estructura antes de la refactorización
En el proyecto original existía un mayor acoplamiento entre la interfaz gráfica, las clases DAO, la conexión a PostgreSQL y algunas reglas de negocio.

```text

Proyecto_Visual

│

├── Interfaces Swing

│   ├── Login

│   ├── Menú principal

│   ├── IFClientes

│   ├── IFProductos

│   ├── IFUsuarios

│   ├── IFVentas

│   └── Historial

│

├── DAO

│   ├── ClienteDAO

│   ├── ProductoDAO

│   ├── UsuarioDAO

│   └── otras clases de acceso a datos

│

├── Modelos

├── Conexión a PostgreSQL

└── Reportes

```

Flujo simplificado de varios módulos:

```text

Interfaz Swing

      ↓

     DAO

      ↓

PostgreSQL

```

Esta organización hacía que algunas vistas conocieran directamente componentes de persistencia y que ciertas validaciones estuvieran distribuidas entre la interfaz y el acceso a datos.

### 5.2 Estructura después de la refactorización
La estructura actual separa presentación, reglas de negocio, contratos de persistencia e implementaciones JDBC:

```text

sistema-ventas-refactor

│

├── src

│   ├── main

│   │   ├── java/ec/edu/uta/ventas

│   │   │   ├── config

│   │   │   │   └── DatabaseConfig

│   │   │   ├── database

│   │   │   │   ├── ConnectionProvider

│   │   │   │   └── PostgresConnectionProvider

│   │   │   ├── model

│   │   │   │   ├── Cliente

│   │   │   │   ├── Producto

│   │   │   │   ├── Usuario

│   │   │   │   ├── Venta

│   │   │   │   └── DetalleVenta

│   │   │   ├── repository

│   │   │   │   ├── ClienteRepository

│   │   │   │   ├── ProductoRepository

│   │   │   │   ├── UsuarioRepository

│   │   │   │   ├── ConfiguracionRepository

│   │   │   │   ├── VentaRepository

│   │   │   │   └── jdbc

│   │   │   │       ├── ClienteJdbcRepository

│   │   │   │       ├── ProductoJdbcRepository

│   │   │   │       ├── UsuarioJdbcRepository

│   │   │   │       ├── ConfiguracionJdbcRepository

│   │   │   │       └── VentaJdbcRepository

│   │   │   ├── security

│   │   │   │   ├── PasswordEncoder

│   │   │   │   └── Sha256PasswordEncoder

│   │   │   ├── service

│   │   │   │   ├── AutenticacionService

│   │   │   │   ├── ClienteService

│   │   │   │   ├── ProductoService

│   │   │   │   ├── UsuarioService

│   │   │   │   ├── ConfiguracionService

│   │   │   │   ├── VentaService

│   │   │   │   └── ReporteFacturaService

│   │   │   ├── session

│   │   │   │   └── SesionActiva

│   │   │   ├── view

│   │   │   │   ├── LoginView

│   │   │   │   ├── MenuPrincipalView

│   │   │   │   ├── ClienteView

│   │   │   │   ├── ProductoView

│   │   │   │   ├── UsuarioView

│   │   │   │   ├── VentaView

│   │   │   │   ├── HistorialVentasView

│   │   │   │   ├── ConfiguracionView

│   │   │   │   └── style/EstiloUI

│   │   │   └── Main

│   │   └── resources

│   │       ├── application.properties

│   │       └── Reportes

│   └── test/java/ec/edu/uta/ventas

│       ├── security

│       ├── service

│       └── session

│

├── .gitignore

├── pom.xml

└── README.md

```

Flujo principal actual:

```text

View

  ↓

Service

  ↓

Repository

  ↑

Repository JDBC

  ↓

ConnectionProvider

  ↓

PostgreSQL

```

`Main` funciona como punto de composición: crea las implementaciones concretas y proporciona las dependencias a servicios y vistas mediante constructores.

### 5.3 Diferencias principales
| Antes | Después |

|---|---|

| Vistas acopladas a clases DAO | Vistas dependientes de servicios |

| Acceso a datos mediante clases concretas | Interfaces `Repository` con implementaciones JDBC |

| Validaciones distribuidas | Reglas principales centralizadas en servicios |

| Conexión más acoplada a persistencia | `ConnectionProvider` abstrae la conexión |

| Autenticación ligada al mecanismo concreto | `PasswordEncoder` abstrae el procesamiento de contraseñas |

| Sesión y permisos menos separados | `SesionActiva` concentra el usuario autenticado |

| Reportes conocidos desde el flujo de interfaz | `ReporteFacturaService` encapsula JasperReports |

| Configuración visual repetida | `EstiloUI` reutiliza estilos comunes |

| Mayor dificultad para pruebas aisladas | Servicios comprobables con repositorios falsos |

La nueva estructura no busca agregar capas innecesarias. Las abstracciones se incorporaron donde reducen acoplamiento o facilitan las pruebas, evitando sobreingeniería.

## 6. Estrategia de refactorización
La refactorización se realizó progresivamente para evitar modificar todas las funcionalidades simultáneamente.

Orden inicial:

1. Configuración del proyecto.

2. Configuración de PostgreSQL.

3. Módulo de clientes.

4. Módulo de productos.

5. Módulo de usuarios.

6. Autenticación.

7. Configuración del sistema.

8. Módulo de ventas.

9. Historial de ventas.

10. Reportes.

11. Pruebas finales.

## 7. Arquitectura implementada
El proyecto utiliza una separación de responsabilidades basada en:

View -> Service -> Repository -> Repository JDBC -> PostgreSQL

### View
Responsable de la interfaz gráfica y la interacción con el usuario.

### Service
Contiene las reglas y casos de uso del negocio.

### Repository
Define contratos para el acceso a los datos.

### Repository JDBC
Implementa los contratos de Repository utilizando JDBC y PostgreSQL.

### Model
Representa las entidades principales del dominio.

## 8. Registro de refactorización
### Etapa 1 - Inicialización del proyecto
Se creó un nuevo proyecto Java utilizando Maven con el objetivo de realizar la refactorización de manera independiente al código original.

Esta decisión permite migrar progresivamente las funcionalidades, manteniendo el proyecto anterior como referencia y evitando trasladar

directamente problemas de diseño existentes.

### Etapa 2 - Configuración del acceso a PostgreSQL
Se incorporó el driver JDBC de PostgreSQL como dependencia Maven.

La configuración de acceso a la base de datos se separó del código Java mediante el archivo `application.properties`.

Para evitar que las clases de persistencia dependan directamente de una clase de conexión estática, se creó la interfaz `ConnectionProvider`.

La implementación `PostgresConnectionProvider` es responsable de obtener conexiones JDBC utilizando la configuración proporcionada por

`DatabaseConfig`.

La estructura resultante es:

ConnectionProvider

        ↑

PostgresConnectionProvider

        ↓

DatabaseConfig

        ↓

application.properties

#### Principios SOLID aplicados
**\*\*\\\*\\\*SRP - Single Responsibility Principle\\\*\\\*\*\***

`DatabaseConfig` tiene la responsabilidad de cargar la configuración,
mientras que `PostgresConnectionProvider` se encarga de proporcionar
conexiones a PostgreSQL.

**\*\*\\\*\\\*DIP - Dependency Inversion Principle\\\*\\\*\*\***

Los repositorios no dependen directamente de una clase de
conexión concreta. Dependen de la abstracción `ConnectionProvider`.

**\*\*\\\*\\\*ISP - Interface Segregation Principle\\\*\\\*\*\***

`ConnectionProvider` define un contrato pequeño y específico con una sola

operación necesaria para obtener conexiones.

#### Antipatrones STUPID reducidos
**\*\*\\\*\\\*T - Tight Coupling\\\*\\\*\*\***

Se reduce el acoplamiento al evitar que los repositorios utilicen
directamente una implementación concreta o una conexión global estática.

**\*\*\\\*\\\*U - Untestability\\\*\\\*\*\***

El uso de una abstracción permitirá sustituir el proveedor de conexiones por otras implementaciones durante las pruebas.

### Etapa 3 - Refactorización del módulo Clientes
El módulo de clientes fue el primer módulo funcional migrado desde el proyecto original.

#### Problemas identificados
En la implementación original, `ClienteDAO` dependía directamente de una clase de conexión estática y también contenía responsabilidades relacionadas con la interfaz gráfica mediante el uso de `JOptionPane`.

Además, algunas operaciones de persistencia contenían lógica y código repetido para tareas similares.

#### Estructura refactorizada
El módulo se separó en:

- `Cliente`: modelo del dominio.

- `ClienteRepository`: contrato de persistencia.

- `ClienteJdbcRepository`: implementación de persistencia mediante JDBC.

- `ClienteService`: reglas y validaciones relacionadas con clientes.

El flujo resultante es:

ClienteView -> ClienteService -> ClienteRepository

                                      ↑

                            ClienteJdbcRepository

                                      ↓

                                  PostgreSQL

#### SOLID aplicado
**\*\*\\\*\\\*SRP - Single Responsibility Principle\\\*\\\*\*\***

Las responsabilidades de presentación, reglas de negocio y persistencia se separaron. El repositorio se encarga del acceso a datos y el servicio de las reglas relacionadas con clientes.

**\*\*\\\*\\\*DIP - Dependency Inversion Principle\\\*\\\*\*\***

`ClienteService` depende de `ClienteRepository` y no directamente de `ClienteJdbcRepository`.

De la misma manera, `ClienteJdbcRepository` obtiene conexiones mediante la abstracción `ConnectionProvider`.

**\*\*\\\*\\\*ISP - Interface Segregation Principle\\\*\\\*\*\***

Los contratos utilizados por el módulo contienen únicamente las operaciones necesarias para sus responsabilidades.

#### STUPID reducido
**\*\*\\\*\\\*T - Tight Coupling\\\*\\\*\*\***

Se eliminó la dependencia directa entre la lógica del módulo y una clase de conexión estática. También se retiró `JOptionPane` de la capa de persistencia.

**\*\*\\\*\\\*U - Untestability\\\*\\\*\*\***

`ClienteService` recibe su repositorio mediante el constructor, lo que

permite probar sus reglas utilizando otra implementación del repositorio sin necesidad de acceder a PostgreSQL.

**\*\*\\\*\\\*D - Duplication\\\*\\\*\*\***

Las operaciones comunes de persistencia fueron extraídas a métodos
privados reutilizables, como el cambio de estado y el mapeo de resultados.

#### Separación de la interfaz gráfica
Se creó `ClienteView` como responsable de la presentación del módulo.
La vista recibe `ClienteService` mediante el constructor y no crea
directamente repositorios ni conexiones a PostgreSQL.
De esta manera, la interfaz gráfica desconoce los detalles de persistencia.

El flujo actual del módulo es:

ClienteView

    ↓

ClienteService

    ↓

ClienteRepository

    ↑

ClienteJdbcRepository

    ↓

ConnectionProvider

    ↓

PostgreSQL

Los componentes Swing, incluyendo `JOptionPane`, permanecen en la capa
de presentación y no forman parte de las clases responsables del acceso
a datos.

La creación y ensamblaje de las dependencias se realiza en `Main`,
evitando introducir contenedores de inyección de dependencias o fábricas innecesarias para el tamaño actual del proyecto.

#### Funcionalidades migradas
La refactorización del módulo mantuvo las principales funcionalidades de
gestión de clientes:

- Listado de clientes activos.

- Búsqueda dinámica de clientes.

- Registro de nuevos clientes.

- Edición de clientes existentes.

- Desactivación lógica de clientes.

- Listado de clientes inactivos.

- Reactivación de clientes.

La desactivación se realiza mediante el campo `activo`, por lo que los
registros no son eliminados físicamente de la base de datos.
La misma vista permite alternar entre clientes activos e inactivos,
evitando crear ventanas adicionales para funcionalidades relacionadas.

#### Validaciones del módulo
Las reglas relacionadas con los datos del cliente se encuentran en
`ClienteService`, evitando depender de errores generados directamente por
PostgreSQL para validar los datos ingresados.

Actualmente se comprueba:

- La cédula es obligatoria.

- La cédula debe contener exactamente 10 dígitos.

- La cédula debe contener únicamente números.

- No se permite registrar una cédula duplicada.

- El nombre es obligatorio.

- El apellido es obligatorio.

- El teléfono es obligatorio.

- El teléfono debe contener exactamente 10 dígitos.

- El teléfono debe contener únicamente números.

- Las operaciones de actualización, desactivación y reactivación requieren

  un identificador válido.

Las validaciones se mantienen dentro de `ClienteService` debido a que,
por el tamaño actual del módulo, crear componentes adicionales exclusivamente
para validación introduciría complejidad sin una necesidad concreta.
Si las reglas aumentan considerablemente o necesitan reutilizarse en otros
componentes, se podrá evaluar posteriormente su extracción.

#### Pruebas unitarias
Se incorporaron pruebas unitarias para `ClienteService` utilizando JUnit 5.

Las pruebas se encuentran en:

```text

src/test/java/ec/edu/uta/ventas/service/ClienteServiceTest.java

```

Para probar el servicio de forma aislada se utiliza una implementación falsa

de `ClienteRepository` dentro de las pruebas.

Durante la ejecución normal:

```text

ClienteService

      ↓

ClienteRepository

      ↑

ClienteJdbcRepository

      ↓

PostgreSQL

```

Durante las pruebas:

```text

ClienteService

      ↓

ClienteRepository

      ↑

ClienteRepositoryFalso

      ↓

List\\<Cliente>

```

Esto permite comprobar las reglas de negocio sin iniciar la interfaz Swing
y sin establecer una conexión con PostgreSQL.
Entre los casos probados se encuentran:

- Registro de un cliente válido.

- Rechazo de cédulas con menos de 10 dígitos.

- Rechazo de cédulas con más de 10 dígitos.

- Rechazo de cédulas con caracteres no numéricos.

- Rechazo de teléfonos inválidos.

- Rechazo de nombres vacíos.

- Rechazo de apellidos vacíos.

- Rechazo de cédulas duplicadas.

- Actualización de un cliente válido.

- Rechazo de actualización con identificador inválido.

- Desactivación de clientes.

- Reactivación de clientes.

- Rechazo de identificadores inválidos.

Las pruebas se ejecutan mediante:

```bash

mvn test

```

Resultado obtenido para `ClienteServiceTest`:

```text

Tests run: 14

Failures: 0

Errors: 0

Skipped: 0

```

Incluyendo la prueba inicial existente en el proyecto, Maven ejecutó:

```text

Tests run: 15

Failures: 0

Errors: 0

Skipped: 0

BUILD SUCCESS

```

Este resultado proporciona evidencia de la mejora en la capacidad de prueba
del módulo después de reducir el acoplamiento entre `ClienteService` y la
implementación concreta de persistencia.

#### Estado del módulo Clientes
El módulo Clientes se considera funcionalmente migrado y refactorizado en
esta etapa.

Las operaciones de creación, consulta, búsqueda, actualización,
desactivación y reactivación fueron comprobadas utilizando PostgreSQL.

Además, las reglas principales de `ClienteService` fueron verificadas mediante
pruebas unitarias independientes de la base de datos.

### Etapa 4 - Refactorización del módulo Productos
Después de completar la migración del módulo Clientes, se inició la
refactorización del módulo Productos.
La estrategia utilizada mantiene la misma separación de responsabilidades
establecida en el módulo anterior.

#### Problemas identificados
En la implementación original, el acceso a los productos se realizaba

mediante una clase DAO que dependía directamente de la clase utilizada

para establecer conexiones con PostgreSQL.

Además, las responsabilidades de persistencia, validación y presentación

se encontraban acopladas entre diferentes componentes del módulo.

La interfaz gráfica también dependía directamente de la implementación

encargada del acceso a datos, dificultando la sustitución de dicha

implementación y la realización de pruebas aisladas.

#### Estructura implementada
El módulo Productos fue separado en:

- `Producto`: modelo del dominio.

- `ProductoRepository`: contrato para las operaciones de persistencia.

- `ProductoJdbcRepository`: implementación del repositorio mediante JDBC.

- `ProductoService`: reglas y validaciones relacionadas con productos.

- `ProductoView`: presentación e interacción con el usuario.

El flujo actual del módulo es:

```text

ProductoView

     ↓

ProductoService

     ↓

ProductoRepository

     ↑

ProductoJdbcRepository

     ↓

ConnectionProvider

     ↓

PostgreSQL

#### Capa de servicio

Se creó `ProductoService` para centralizar las reglas de negocio y

validaciones relacionadas con los productos.

El servicio recibe `ProductoRepository` mediante el constructor, por lo

que no depende directamente de `ProductoJdbcRepository` ni de PostgreSQL.

```text

ProductoService

       ↓

ProductoRepository

       ↑

ProductoJdbcRepository

#### Pruebas unitarias

Se incorporaron pruebas unitarias para `ProductoService` utilizando JUnit 5.

Las pruebas se encuentran en:

```text

src/test/java/ec/edu/uta/ventas/service/ProductoServiceTest.java

#### Separación de la interfaz gráfica

La interfaz del módulo Productos fue adaptada para depender de

`ProductoService` en lugar de crear directamente una instancia de la clase

encargada del acceso a datos.

Anteriormente, la interfaz realizaba operaciones como:

- Consultar directamente el DAO.

- Comprobar códigos duplicados.

- Comprobar códigos de barras duplicados.

- Construir objetos para persistencia.

- Ejecutar directamente operaciones de registro y actualización.

Después de la refactorización, estas responsabilidades se delegan a

`ProductoService`.

El flujo de la interfaz queda definido como:

```text

ProductoView

     ↓

ProductoService

     ↓

ProductoRepository

     ↑

ProductoJdbcRepository

     ↓

ConnectionProvider

     ↓

PostgreSQL

El módulo de productos fue migrado de la estructura original a la arquitectura
basada en View, Service y Repository.

En la implementación original, la vista `IFProductos` utilizaba directamente
`ProductoDAO`, por lo que la interfaz gráfica estaba acoplada al acceso a datos.
Además, parte de las validaciones y comprobaciones de duplicados se realizaban
desde la propia vista.

La estructura refactorizada es:

ProductoView

    ↓

ProductoService

    ↓

ProductoRepository

    ↑

ProductoJdbcRepository

    ↓

ConnectionProvider

    ↓

PostgreSQL

#### Componentes creados

- `Producto`: representa los datos y comportamiento básico de un producto.

- `ProductoRepository`: define el contrato de persistencia del módulo.

- `ProductoJdbcRepository`: implementa el acceso a PostgreSQL mediante JDBC.

- `ProductoService`: contiene las reglas y validaciones del módulo.

- `ProductoView`: contiene únicamente la interacción con el usuario y delega

  las operaciones al servicio.

#### Funcionalidades migradas

Se verificaron las siguientes funcionalidades:

- Listado de productos activos.

- Búsqueda dinámica por código, código de barras o nombre.

- Generación automática del código del producto.

- Registro de productos.

- Edición de productos.

- Desactivación lógica.

- Listado de productos inactivos.

- Reactivación de productos.

- Código de barras opcional.

- Control de código de barras duplicado.

La eliminación continúa siendo lógica mediante el campo `activo`, por lo que

los registros no son eliminados físicamente de la base de datos.

#### Validaciones del módulo

Las reglas de negocio fueron centralizadas en `ProductoService`.

Actualmente se valida:

- El nombre es obligatorio.

- El precio es obligatorio.

- El precio debe ser mayor que cero.

- El stock no puede ser negativo.

- El identificador debe ser válido.

- Si se proporciona un código de barras, este no puede pertenecer a otro producto.

- Un producto activo no puede reactivarse.

- Un producto inactivo no puede volver a desactivarse.

El código de barras vacío se normaliza a `null`.

Las validaciones se mantienen dentro de `ProductoService` porque actualmente

son específicas del módulo y no existe una necesidad concreta de crear una

clase de validación adicional.

#### Aplicación de SOLID

\*\*SRP - Single Responsibility Principle\*\*

Las responsabilidades fueron separadas entre presentación, reglas de negocio

y persistencia.

`ProductoView` administra la interacción con el usuario, `ProductoService`

gestiona las reglas del módulo y `ProductoJdbcRepository` realiza las

operaciones JDBC.

\*\*DIP - Dependency Inversion Principle\*\*

`ProductoService` depende de la abstracción `ProductoRepository` y no de

`ProductoJdbcRepository`.

Asimismo, `ProductoJdbcRepository` utiliza la abstracción

`ConnectionProvider` para obtener conexiones.

Esto permite reemplazar las implementaciones concretas sin modificar la

lógica del servicio.

\*\*ISP - Interface Segregation Principle\*\*

`ProductoRepository` mantiene un contrato enfocado en las operaciones de

persistencia necesarias para el módulo de productos.

A medida que se incorporen nuevos módulos se evaluará si existen interfaces

que obliguen a sus implementaciones a depender de operaciones que no utilizan.

#### Reducción de antipatrones STUPID

\*\*T - Tight Coupling\*\*

La vista ya no instancia ni utiliza directamente `ProductoDAO`.

Las dependencias son proporcionadas mediante constructores y las capas se

comunican mediante abstracciones.

\*\*U - Untestability\*\*

La lógica de productos puede probarse sin ejecutar Swing y sin conectarse a

PostgreSQL.

Durante las pruebas se utiliza:

ProductoService

    ↓

ProductoRepository

    ↑

ProductoRepositoryFalso

\*\*D - Duplication\*\*

En `ProductoJdbcRepository` se reutilizan métodos privados para operaciones

comunes como:

- listado de productos;

- búsqueda de un producto;

- cambio de estado;

- mapeo de `ResultSet` a `Producto`.

Esto evita repetir código JDBC innecesariamente.

#### Pruebas unitarias

Se creó:

`src/test/java/ec/edu/uta/ventas/service/ProductoServiceTest.java`

Las pruebas utilizan JUnit 5 y una implementación falsa de

`ProductoRepository`, sin utilizar Mockito ni una base de datos real.

Se verifican, entre otros casos:

- Registro válido.

- Nombre obligatorio.

- Precio obligatorio.

- Precio mayor que cero.

- Stock no negativo.

- Stock igual a cero permitido.

- Código de barras duplicado.

- Código de barras vacío.

- Actualización válida.

- Actualización conservando el mismo código de barras.

- Rechazo del código de barras perteneciente a otro producto.

- Identificadores inválidos.

- Producto inexistente.

- Desactivación.

- Reactivación.

- Estados inválidos de activación/desactivación.

Las pruebas se ejecutaron mediante:

```bash

mvn test

## Etapa 5 - Refactorización del módulo Usuarios

### Objetivo

Refactorizar el módulo de administración de usuarios para reducir el

acoplamiento entre la interfaz gráfica, la lógica de negocio y el acceso

a la base de datos.

La estructura implementada es:

UsuarioView

    ↓

UsuarioService

    ↓

UsuarioRepository

    ↓

UsuarioJdbcRepository

    ↓

PostgreSQL

### Cambios realizados

Se crearon los siguientes componentes:

- `model/Usuario.java`

- `repository/UsuarioRepository.java`

- `repository/jdbc/UsuarioJdbcRepository.java`

- `service/UsuarioService.java`

- `view/UsuarioView.java`

La vista ya no realiza consultas SQL ni utiliza directamente un DAO.

Todas las operaciones pasan por `UsuarioService`.

### Funcionalidades migradas

El módulo permite:

- Listar usuarios.

- Buscar usuarios por nombre, apellido o username.

- Registrar nuevos usuarios.

- Generar automáticamente el username desde los datos ingresados.

- Editar nombre, apellido y rol.

- Mantener el username bloqueado durante la edición.

- Desactivar usuarios mediante borrado lógico.

- Cambiar la contraseña de un usuario.

- Diferenciar los roles `ADMIN` y `CAJERO`.

- Evitar la desactivación del administrador principal.

- Evitar usernames duplicados.

La edición utiliza dos estados en la interfaz:

1. Al seleccionar un usuario, sus datos se muestran bloqueados.

2. Al presionar `Editar`, se habilitan los campos permitidos y el botón

   cambia a `Actualizar`.

Esto evita modificaciones accidentales de la información mostrada.

### Validaciones

Las principales reglas de negocio se encuentran en `UsuarioService`:

- Nombre obligatorio.

- Apellido obligatorio.

- Username obligatorio.

- Username único.

- Contraseña obligatoria.

- Contraseña de mínimo 6 caracteres.

- Rol limitado a `ADMIN` o `CAJERO`.

- ID de usuario válido.

- El username no puede modificarse durante la actualización.

- El administrador principal no puede ser desactivado.

- No se puede desactivar nuevamente un usuario que ya está inactivo.

Las validaciones relacionadas únicamente con la interacción gráfica,

como la confirmación de contraseña, permanecen en `UsuarioView`.

### Aplicación de SOLID

#### SRP - Single Responsibility Principle

Las responsabilidades fueron separadas:

- `UsuarioView`: interacción con el usuario.

- `UsuarioService`: reglas de negocio y validaciones.

- `UsuarioRepository`: contrato de persistencia.

- `UsuarioJdbcRepository`: acceso a PostgreSQL.

- `Usuario`: representación del usuario.

Esto evita concentrar interfaz, SQL y reglas de negocio en una misma clase.

#### DIP - Dependency Inversion Principle

`UsuarioService` depende de la abstracción:

`UsuarioRepository`

y no directamente de:

`UsuarioJdbcRepository`

De esta manera la lógica de negocio puede probarse sin una conexión real

a PostgreSQL.

### Reducción de problemas STUPID

#### Tight Coupling

Se redujo el acoplamiento directo entre la interfaz y el acceso a datos.

Antes:

View → DAO → Base de datos

Ahora:

View → Service → Repository → Implementación JDBC

#### Untestability

La lógica del módulo puede probarse utilizando una implementación falsa

de `UsuarioRepository`, sin necesidad de iniciar Swing ni PostgreSQL.

#### Duplication

Las reglas de validación se concentran en `UsuarioService` en lugar de

repetirse en distintos eventos de la interfaz.

### Pruebas

Se creó:

`src/test/java/ec/edu/uta/ventas/service/UsuarioServiceTest.java`

Las pruebas cubren, entre otros casos:

- Registro válido.

- Nombre obligatorio.

- Apellido obligatorio.

- Username obligatorio.

- Username duplicado.

- Contraseña obligatoria.

- Longitud mínima de contraseña.

- Roles válidos e inválidos.

- Actualización de usuario.

- Protección del username durante la edición.

- Usuario inexistente.

- Desactivación.

- Protección del administrador principal.

- Usuario previamente desactivado.

- Cambio de contraseña.

- IDs inválidos.

Las pruebas utilizan un repositorio falso en memoria, evitando depender

de PostgreSQL.

### Estado del módulo

\| Componente | Estado |

\|---|---|

\| Modelo Usuario | Completado |

\| UsuarioRepository | Completado |

\| UsuarioJdbcRepository | Completado |

\| UsuarioService | Completado |

\| UsuarioView | Completado |

\| Búsqueda dinámica | Completado |

\| Registro | Completado |

\| Edición | Completado |

\| Desactivación lógica | Completado |

\| Cambio de contraseña | Completado |

\| Pruebas unitarias | Completado |

### Deuda técnica identificada

Actualmente las contraseñas mantienen el mecanismo SHA-256 utilizado

por el sistema existente para conservar compatibilidad durante la

refactorización.

Este mecanismo no se considera adecuado para almacenamiento moderno de

contraseñas debido a que SHA-256 es una función de hash rápida y no está

diseñada específicamente para contraseñas.

La sustitución por un algoritmo apropiado para contraseñas se evaluará

al refactorizar el módulo de autenticación.

## Etapa 6 - Autenticación, sesión y configuración del sistema

### Objetivo

Refactorizar el proceso de autenticación y la gestión de configuraciones

del sistema, eliminando dependencias directas entre las vistas y el acceso

a datos.

### Autenticación

Se implementó la siguiente separación:

LoginView

    ↓

AutenticacionService

    ↓

UsuarioRepository

    ↓

UsuarioJdbcRepository

    ↓

PostgreSQL

La sesión del usuario autenticado se administra mediante `SesionActiva`.

`SesionActiva` mantiene únicamente información relacionada con el usuario

que inició sesión y sus permisos.

Los roles utilizados actualmente son:

- ADMIN

- CAJERO

El menú principal utiliza la sesión activa para determinar qué módulos

puede visualizar cada usuario.

### Navegación

La creación de las dependencias principales se realiza en `Main`.

Las vistas no crean directamente repositorios ni servicios.

El flujo principal es:

Main

 ├── Repositories

 ├── Services

 ├── SesionActiva

 ├── LoginView

 └── MenuPrincipalView

Después de una autenticación correcta se abre `MenuPrincipalView`.

Al cerrar sesión:

MenuPrincipalView

    ↓

AutenticacionService.cerrarSesion()

    ↓

SesionActiva

    ↓

LoginView

### Abstracción del mecanismo de contraseñas

Para reducir el acoplamiento entre la lógica de usuarios/autenticación y el algoritmo utilizado para procesar contraseñas, se creó la interfaz `PasswordEncoder`.

```text

PasswordEncoder

      ↑

Sha256PasswordEncoder

```

`UsuarioService` y `AutenticacionService` reciben `PasswordEncoder` mediante el constructor. Esto aplica DIP porque los servicios dependen de una abstracción y no de `Sha256PasswordEncoder`.

La implementación actual conserva SHA-256 para mantener compatibilidad con los usuarios existentes. La migración a BCrypt, Argon2 u otro algoritmo adaptativo permanece como deuda técnica.

### Configuración del sistema
Se creó el módulo de configuración utilizando:

ConfiguracionView

    ↓

ConfiguracionService

    ↓

ConfiguracionRepository

    ↓

ConfiguracionJdbcRepository

    ↓

PostgreSQL

Las configuraciones son obtenidas desde la tabla `configuracion`.

Actualmente se administran parámetros como:

- IVA

- STOCK_MINIMO

- nombre de la empresa

- RUC de la empresa

- dirección de la empresa

La validación de estos valores se encuentra en `ConfiguracionService`.

### Integración con Productos
`ProductoView` recibe `ConfiguracionService` mediante su constructor.

El valor de `STOCK_MINIMO` ya no pertenece a la sesión del usuario.

ProductoView

    ↓

ConfiguracionService

    ↓

ConfiguracionRepository

    ↓

PostgreSQL

El stock mínimo se carga al abrir la ventana de Productos.

Los productos cuyo stock sea menor o igual al valor configurado como

`STOCK_MINIMO` son resaltados en la tabla.

De esta manera existe una única fuente para la configuración del stock

mínimo.

### Permisos
ADMIN:

- Productos

- Clientes

- Usuarios

- Ventas

- Configuración

- Sistema

CAJERO:

- Clientes

- Ventas

- Sistema

El módulo Configuración solamente se encuentra disponible para ADMIN.

### Pruebas
Se agregaron pruebas unitarias para:

- autenticación

- manejo de sesión

- consulta de configuraciones

- actualización del IVA

- actualización del stock mínimo

- validación del RUC

- datos de la empresa

- configuraciones inexistentes

- valores inválidos

Las pruebas utilizan repositorios falsos para probar la lógica de los

servicios sin depender directamente de PostgreSQL.

### Mejoras obtenidas
- Las vistas no acceden directamente a PostgreSQL.

- La autenticación está separada de la interfaz.

- La sesión solamente mantiene información del usuario autenticado.

- La configuración tiene su propio Repository y Service.

- El stock mínimo se obtiene desde la configuración del sistema.

- Las dependencias son proporcionadas desde `Main`.

- Se facilita la realización de pruebas unitarias.

- Se reduce el acoplamiento entre interfaz, lógica y persistencia.

### Deuda técnica pendiente
La autenticación conserva temporalmente SHA-256 para mantener

compatibilidad con los usuarios existentes.

La migración a un mecanismo específico para almacenamiento seguro de

contraseñas queda como mejora posterior.

## Etapa 7 - Refactorización del módulo de Ventas y Facturación
Se refactorizó el módulo de ventas con el objetivo de separar la interfaz gráfica,

la lógica de negocio, el acceso a datos y la generación de reportes.

### Arquitectura aplicada
El flujo principal del módulo quedó organizado de la siguiente manera:

View → Service → Repository → PostgreSQL

Para la generación de facturas:

View → ReporteFacturaService → JasperReports → PostgreSQL

### Modelo de ventas
Se implementaron los modelos:

- `Venta`

- `DetalleVenta`

`Venta` representa la cabecera de la venta y contiene información como:

- Número de factura.

- Fecha.

- Cliente.

- Usuario.

- Subtotal.

- Porcentaje de IVA.

- IVA.

- Total.

- Estado de anulación.

- Detalles de la venta.

`DetalleVenta` representa cada producto incluido en una venta y contiene:

- Producto.

- Cantidad.

- Precio unitario.

- Subtotal del producto.

### VentaRepository
Se creó la abstracción `VentaRepository` para evitar que la capa de negocio

dependa directamente de JDBC.

Entre sus operaciones se encuentran:

- Guardar una venta.

- Buscar por ID.

- Buscar por número de factura.

- Listar ventas.

- Buscar ventas por fecha.

- Listar detalles de una venta.

- Anular una venta.

### VentaJdbcRepository
Se implementó `VentaJdbcRepository` como implementación JDBC de

`VentaRepository`.

El registro de una venta utiliza una transacción de PostgreSQL.

Dentro de la misma transacción se realizan las siguientes operaciones:

1. Generación del número de factura.

2. Registro de la cabecera de la venta.

3. Bloqueo y validación del stock de los productos.

4. Registro de los detalles de la venta.

5. Actualización del stock.

6. Confirmación de la transacción mediante `commit`.

Si alguna operación falla, se ejecuta `rollback`.

El bloqueo de productos permite volver a comprobar el stock dentro de la

transacción y ayuda a controlar ventas concurrentes.

### VentaService
Se creó `VentaService` para concentrar las reglas de negocio relacionadas

con las ventas.

Sus responsabilidades incluyen:

- Validar los datos de la venta.

- Obtener el usuario desde la sesión activa.

- Obtener el IVA desde `ConfiguracionService`.

- Calcular subtotal, IVA y total.

- Registrar la venta.

- Consultar ventas.

- Consultar el detalle de una venta.

- Anular ventas.

La interfaz gráfica ya no realiza directamente las operaciones JDBC.

### Anulación de ventas
La anulación se realiza mediante `VentaService` y `VentaRepository`.

Al anular una venta:

- Se valida que exista una sesión activa.

- Se verifica que el usuario tenga rol `ADMIN`.

- Se restauran las cantidades de los productos al stock.

- La venta se marca como anulada.

La operación se realiza mediante una transacción para mantener la

consistencia de los datos.

Se eliminó de la nueva implementación el PIN fijo utilizado anteriormente

para realizar anulaciones.

### Historial de ventas
Se implementó `HistorialVentasView`.

Permite:

- Listar todas las ventas.

- Buscar por número de factura.

- Buscar por fecha.

- Visualizar el detalle de una venta.

- Identificar ventas anuladas.

- Anular ventas cuando el usuario tiene permisos de administrador.

- Reimprimir una factura.

La visualización del detalle utiliza la información histórica almacenada en

`detalle_ventas`, incluyendo el precio unitario registrado al momento de la

venta.

### JasperReports
Se integró JasperReports mediante Maven para la generación y visualización

de facturas.

Se reutilizaron los recursos del reporte existente:

src/main/resources/Reportes/factura.jrxml

src/main/resources/Reportes/logos.png

Se creó `ReporteFacturaService`, encargado de:

- Obtener la conexión mediante `ConnectionProvider`.

- Obtener los datos de la empresa mediante `ConfiguracionService`.

- Cargar el archivo `factura.jrxml`.

- Cargar el logo desde los recursos de la aplicación.

- Compilar el reporte.

- Enviar los parámetros requeridos.

- Generar la factura.

- Mostrarla mediante `JasperViewer`.

De esta manera, la vista no necesita conocer detalles de JasperReports,

JDBC o la configuración de la empresa.

### Reimpresión de facturas
Desde `HistorialVentasView` se puede seleccionar una venta y utilizar la

opción:

`Reimprimir factura`

La vista envía únicamente el ID de la venta a `ReporteFacturaService`,

que se encarga de generar nuevamente el reporte.

### Factura después de registrar una venta
Después de registrar correctamente una venta, el sistema pregunta:

`¿Desea visualizar la factura?`

Si el usuario selecciona `Sí`, se utiliza el mismo `ReporteFacturaService`

para mostrar la factura recién generada.

La generación del reporte se maneja independientemente del registro de la

venta. Por esta razón, si JasperReports presenta un error después de que la

venta fue almacenada correctamente, el sistema no considera que la venta

haya fallado.

### Dependencias agregadas
Se agregaron mediante Maven:

- JasperReports.

- Groovy, requerido para compilar las expresiones utilizadas por el

  archivo JRXML original.

No se utilizan archivos JAR agregados manualmente al proyecto.

### Principios aplicados durante la refactorización
La refactorización permitió reducir varios problemas presentes en la

implementación anterior:

- La interfaz gráfica dejó de acceder directamente a la base de datos.

- Se redujo el acoplamiento entre Swing y JDBC.

- Las reglas de negocio se trasladaron a servicios.

- El acceso a datos quedó detrás de interfaces Repository.

- Las dependencias se reciben mediante constructores.

- La conexión a PostgreSQL se abstrajo mediante `ConnectionProvider`.

- La generación de reportes quedó separada de las vistas.

- Se reutiliza `ReporteFacturaService` tanto para nuevas ventas como para

  reimpresión.

- Se mejoró la capacidad de realizar pruebas unitarias sobre la lógica de

  negocio.

### Pruebas realizadas
Se verificó correctamente:

- Registro de ventas.

- Actualización del stock.

- Validación de stock insuficiente.

- Manejo de concurrencia durante la venta.

- Cálculo de subtotal, IVA y total.

- Consulta del historial.

- Búsqueda por factura.

- Búsqueda por fecha.

- Visualización del detalle.

- Anulación de ventas.

- Restauración del stock después de una anulación.

- Restricción de anulación según rol.

- Generación de factura mediante JasperReports.

- Reimpresión desde el historial.

- Visualización de factura después de registrar una venta.

- Compilación y ejecución de las pruebas mediante Maven.

## 9. Unificación de la interfaz gráfica
Después de completar la separación funcional se unificó el estilo de las principales vistas Swing sin trasladar reglas de negocio a la presentación.

Se creó `view/style/EstiloUI.java` para reutilizar colores, botones, campos, tablas, paneles, títulos y otros elementos visuales comunes. Se aplicó en `LoginView`, `MenuPrincipalView`, `ClienteView`, `ProductoView`, `UsuarioView`, `VentaView`, `HistorialVentasView` y `ConfiguracionView`.

Esta modificación reduce duplicación visual y mantiene una apariencia consistente sin introducir un framework adicional.

## 10. Aplicación de SOLID
La refactorización se concentró principalmente en SRP, DIP e ISP, que eran los principios con aplicación más clara en los problemas detectados. OCP y LSP se favorecen mediante el uso de abstracciones, pero no se afirma que cada clase del sistema constituya por sí misma una demostración completa de los cinco principios.

### SRP - Single Responsibility Principle
Se separaron responsabilidades que antes estaban mezcladas:

- Las vistas Swing administran interacción y presentación.

- Los servicios concentran reglas de negocio y validaciones.

- Los repositorios JDBC concentran SQL y persistencia.

- `DatabaseConfig` carga configuración.

- `ConnectionProvider` proporciona conexiones.

- `SesionActiva` administra la sesión del usuario.

- `ReporteFacturaService` encapsula JasperReports.

- `EstiloUI` concentra estilos visuales reutilizables.

Ejemplo:

```text

Antes

IFProductos → interfaz + validaciones + DAO

Después

ProductoView → ProductoService → ProductoRepository → ProductoJdbcRepository

```

### DIP - Dependency Inversion Principle
Los servicios dependen de interfaces y no de implementaciones JDBC concretas.

```text

ClienteService      → ClienteRepository

ProductoService     → ProductoRepository

UsuarioService      → UsuarioRepository

VentaService        → VentaRepository

ConfiguracionService→ ConfiguracionRepository

```

Los repositorios JDBC obtienen conexiones mediante `ConnectionProvider`. Además, `UsuarioService` y `AutenticacionService` utilizan la abstracción `PasswordEncoder`.

Las implementaciones concretas se crean en `Main`, que funciona como punto de composición.

### ISP - Interface Segregation Principle
Las interfaces creadas tienen responsabilidades específicas. Por ejemplo, `ConnectionProvider` solamente expone la operación necesaria para obtener conexiones, mientras que cada Repository define las operaciones requeridas por su módulo.

No se creó una interfaz genérica única con métodos que algunos módulos no necesitan.

### OCP - Open/Closed Principle
El uso de interfaces permite incorporar implementaciones alternativas de repositorios, proveedores de conexión o codificadores de contraseña sin obligar a modificar la lógica principal de los servicios.

### LSP - Liskov Substitution Principle
Las implementaciones concretas se utilizan a través de sus contratos. Durante las pruebas, por ejemplo, un repositorio JDBC puede sustituirse por una implementación falsa que respete la misma interfaz sin modificar el servicio probado.

## 11. Reducción de antipatrones STUPID
La refactorización no afirma que todo posible problema STUPID haya desaparecido. Se redujeron específicamente los problemas identificados en el sistema original.

### S - Singleton
Se evitó introducir estado global adicional para resolver dependencias. Las dependencias principales se crean en `Main` y se proporcionan mediante constructores.

`SesionActiva` tiene una responsabilidad concreta relacionada con la sesión y se comparte explícitamente desde el punto de composición, en lugar de convertirse en una dependencia global obtenida desde cualquier clase.

### T - Tight Coupling
Fue uno de los principales problemas reducidos.

**\*\*Antes:\*\***

```text

View → DAO concreto → conexión/base de datos

```

**\*\*Después:\*\***

```text

View → Service → Repository

                    ↑

             Repository JDBC

```

También se introdujeron `ConnectionProvider` y `PasswordEncoder` para evitar dependencias directas con implementaciones concretas.

### U - Untestability
La lógica de negocio fue extraída de las vistas y los servicios reciben sus dependencias mediante constructores. Esto permite probar servicios con repositorios falsos sin abrir Swing ni conectarse a PostgreSQL.

Se añadieron pruebas para clientes, productos, usuarios, autenticación, configuración, sesión, ventas y seguridad.

### P - Premature Optimization
Se evitó introducir arquitecturas, frameworks o patrones sin una necesidad concreta. No se añadió un contenedor de inyección de dependencias y las dependencias continúan ensamblándose explícitamente en `Main`.

Tampoco se dividieron clases únicamente por cantidad de líneas cuando no existía una responsabilidad diferente que justificara otra abstracción.

### I - Indescriptive Naming
Se utilizaron nombres que representan la responsabilidad de los componentes, por ejemplo:

- `ClienteService`

- `ProductoRepository`

- `ProductoJdbcRepository`

- `AutenticacionService`

- `ReporteFacturaService`

- `ConnectionProvider`

- `PasswordEncoder`

- `SesionActiva`

Esto hace más explícito el propósito de cada componente que una estructura basada únicamente en clases genéricas o responsabilidades mezcladas.

### D - Duplication
Se redujo código repetido mediante métodos reutilizables en repositorios y mediante componentes compartidos.

Ejemplos:

- Métodos privados de mapeo y cambio de estado en repositorios JDBC.

- `ReporteFacturaService` reutilizado al generar y reimprimir facturas.

- `EstiloUI` reutilizado por las vistas para evitar repetir configuración visual.

- Validaciones de negocio centralizadas en servicios en lugar de duplicarse en eventos Swing.

## 12. Pruebas finales
La refactorización fue comprobada mediante Maven:

```bash

mvn clean test

```

La ejecución finalizó con:

```text

BUILD SUCCESS

```

Las pruebas cubren los principales servicios y componentes de seguridad/sesión de forma aislada, utilizando implementaciones falsas de Repository cuando corresponde.

Además de las pruebas unitarias se verificaron manualmente las funcionalidades principales: autenticación, permisos, clientes, productos, usuarios, configuración, registro de ventas, actualización de stock, control de concurrencia, historial, anulación y generación/reimpresión de facturas.

## 13. Estado final y deuda técnica
El sistema conserva las funcionalidades principales del proyecto original y presenta una separación más clara entre presentación, reglas de negocio y persistencia.

La creación de dependencias permanece explícita en `Main`, evitando incorporar infraestructura innecesaria para el tamaño actual del proyecto.

La principal deuda técnica identificada es el mecanismo SHA-256 utilizado para las contraseñas. Se mantiene temporalmente por compatibilidad con los datos existentes, pero la abstracción `PasswordEncoder` permite sustituirlo posteriormente por un algoritmo adaptativo específico para contraseñas.

También pueden incorporarse en el futuro pruebas de integración con PostgreSQL y pruebas automatizadas de interfaz si el alcance del sistema aumenta.

## 14. Conclusión
El sistema fue refactorizado para reducir los principales antipatrones STUPID identificados y aplicar los principios SOLID en las responsabilidades críticas, especialmente separación de responsabilidades, inversión de dependencias y uso de abstracciones.

La refactorización no busca afirmar un cumplimiento absoluto de SOLID ni la eliminación total de STUPID. El objetivo fue mejorar la mantenibilidad, capacidad de prueba y claridad del sistema sin introducir sobreingeniería y conservando el comportamiento funcional requerido.
