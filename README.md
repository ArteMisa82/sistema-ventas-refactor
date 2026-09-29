# Sistema de Ventas — Refactorización SOLID y reducción de STUPID

## 1. Descripción

Este proyecto corresponde a la **refactorización de un Sistema de Ventas desarrollado en Java Swing con PostgreSQL**. El trabajo parte de una implementación existente y conserva sus funcionalidades principales, pero reorganiza el código para mejorar su **mantenibilidad, claridad, capacidad de prueba y separación de responsabilidades**.

La refactorización se enfocó en dos objetivos técnicos principales:

1. **Reducir los antipatrones STUPID** identificados en el proyecto original.
2. **Aplicar principios SOLID** donde aportan una mejora concreta al diseño, evitando introducir abstracciones o patrones innecesarios.

El resultado utiliza una organización basada principalmente en:

```text
View → Service → Repository → Repository JDBC → PostgreSQL
```

Las implementaciones concretas se crean en `Main`, que funciona como **punto de composición** y proporciona las dependencias mediante constructores.

---

## 2. Problemas identificados antes de la refactorización

Durante la revisión del proyecto original se encontraron principalmente los siguientes problemas:

- Vistas Swing con acceso directo a clases DAO.
- Dependencia directa de clases concretas para conectarse a PostgreSQL.
- Reglas de negocio y validaciones distribuidas entre vistas y acceso a datos.
- Uso de componentes de interfaz, como `JOptionPane`, dentro de clases relacionadas con persistencia.
- Código JDBC repetido para consultas, mapeo y cambios de estado.
- Componentes difíciles de probar sin ejecutar Swing o conectarse a PostgreSQL.
- Dependencias concretas para autenticación y procesamiento de contraseñas.
- Configuración visual repetida entre diferentes ventanas.
- Responsabilidades mezcladas en algunas clases.

Estos problemas se relacionaban principalmente con **Tight Coupling, Untestability y Duplication** de STUPID.

---

## 3. Reducción de antipatrones STUPID

La refactorización no pretende afirmar que cualquier posible problema STUPID haya desaparecido. Se trabajó específicamente sobre los problemas encontrados en el sistema original.

| Antipatrón | Problema identificado | Refactorización aplicada | Resultado |
|---|---|---|---|
| **S — Singleton** | Riesgo de utilizar estado global para compartir dependencias. | Las dependencias se crean en `Main` y se entregan explícitamente por constructor. `SesionActiva` se comparte como una dependencia controlada. | Menor dependencia de estado global oculto. |
| **T — Tight Coupling** | Vistas dependían directamente de DAO, JDBC y conexión a BD. | Se introdujeron `Service`, interfaces `Repository`, `ConnectionProvider` y `PasswordEncoder`. | Las capas dependen de contratos y responsabilidades más claras. |
| **U — Untestability** | La lógica estaba ligada a Swing y PostgreSQL. | La lógica se trasladó a servicios y se usan repositorios falsos en pruebas. | Los servicios pueden probarse sin abrir la interfaz ni utilizar una BD real. |
| **P — Premature Optimization** | Existía el riesgo de añadir patrones o infraestructura sin necesidad. | Se mantuvo una arquitectura sencilla y el ensamblaje manual en `Main`. | Se evita sobreingeniería para el tamaño actual del proyecto. |
| **I — Indescriptive Naming** | Algunas responsabilidades no eran evidentes por la organización original. | Se adoptaron nombres como `ClienteService`, `VentaRepository`, `ReporteFacturaService` y `ConnectionProvider`. | El propósito de los componentes es más explícito. |
| **D — Duplication** | Validaciones, JDBC y estilos se repetían en distintos puntos. | Se centralizaron reglas en servicios, métodos comunes en repositorios y estilos en `EstiloUI`. | Menos código repetido y un único lugar para modificar reglas comunes. |

### Antes

```text
View → DAO concreto → conexión/base de datos
```

### Después

```text
View
  ↓
Service
  ↓
Repository (interfaz)
  ↑
Repository JDBC
  ↓
ConnectionProvider
  ↓
PostgreSQL
```

---

## 4. Principios SOLID aplicados

La refactorización se concentra principalmente en **SRP, DIP e ISP**, porque son los principios que responden directamente a los problemas detectados. El uso de abstracciones también favorece OCP y LSP.

### 4.1 SRP — Single Responsibility Principle

Las responsabilidades que antes se encontraban mezcladas se distribuyeron de la siguiente manera:

| Componente | Responsabilidad principal |
|---|---|
| `View` | Presentación e interacción con el usuario. |
| `Service` | Casos de uso, reglas de negocio y validaciones. |
| `Repository` | Contrato de persistencia. |
| `Repository JDBC` | SQL y acceso a PostgreSQL. |
| `DatabaseConfig` | Carga de configuración de base de datos. |
| `ConnectionProvider` | Obtención de conexiones. |
| `SesionActiva` | Información de la sesión autenticada. |
| `ReporteFacturaService` | Generación y visualización de facturas con JasperReports. |
| `EstiloUI` | Estilos Swing reutilizables. |

Ejemplo:

```text
Antes:
IFProductos → interfaz + validaciones + DAO

Después:
ProductoView → ProductoService → ProductoRepository → ProductoJdbcRepository
```

### 4.2 DIP — Dependency Inversion Principle

Los servicios dependen de abstracciones y no de implementaciones JDBC concretas:

```text
ClienteService       → ClienteRepository
ProductoService      → ProductoRepository
UsuarioService       → UsuarioRepository
VentaService         → VentaRepository
ConfiguracionService → ConfiguracionRepository
```

Además:

- Los repositorios JDBC reciben `ConnectionProvider`.
- `UsuarioService` y `AutenticacionService` reciben `PasswordEncoder`.
- Las implementaciones concretas se ensamblan en `Main`.

### 4.3 ISP — Interface Segregation Principle

Las interfaces se mantienen enfocadas en las operaciones requeridas por cada módulo. No se creó un repositorio genérico con métodos que algunos módulos no necesitan.

`ConnectionProvider`, por ejemplo, define únicamente la operación necesaria para proporcionar conexiones.

### 4.4 OCP — Open/Closed Principle

Los contratos permiten incorporar implementaciones alternativas de repositorios, proveedores de conexión o mecanismos de codificación de contraseña sin modificar la lógica principal de los servicios.

### 4.5 LSP — Liskov Substitution Principle

Los servicios trabajan contra contratos. Durante las pruebas, una implementación JDBC puede sustituirse por un repositorio falso que respete la misma interfaz sin modificar el servicio probado.

---

## 5. Comparación antes y después

| Antes de la refactorización | Después de la refactorización |
|---|---|
| Vistas acopladas a DAO concretos. | Vistas dependientes de servicios. |
| DAO y conexión utilizados directamente. | Persistencia detrás de interfaces `Repository`. |
| Validaciones distribuidas. | Reglas principales centralizadas en servicios. |
| Conexión a BD fuertemente acoplada. | `ConnectionProvider` abstrae la obtención de conexiones. |
| Autenticación ligada al algoritmo concreto. | `PasswordEncoder` abstrae el procesamiento de contraseñas. |
| Sesión y permisos mezclados con otros componentes. | `SesionActiva` concentra el estado de autenticación. |
| JasperReports conocido por las vistas. | `ReporteFacturaService` encapsula la generación de reportes. |
| Estilos Swing repetidos. | `EstiloUI` reutiliza configuración visual. |
| Pruebas dependientes de infraestructura. | Servicios comprobables mediante repositorios falsos. |

---

## 6. Arquitectura actual

```text
sistema-ventas-refactor/
├── src/
│   ├── main/
│   │   ├── java/ec/edu/uta/ventas/
│   │   │   ├── config/
│   │   │   │   └── DatabaseConfig.java
│   │   │   ├── database/
│   │   │   │   ├── ConnectionProvider.java
│   │   │   │   └── PostgresConnectionProvider.java
│   │   │   ├── model/
│   │   │   │   ├── Cliente.java
│   │   │   │   ├── Producto.java
│   │   │   │   ├── Usuario.java
│   │   │   │   ├── Venta.java
│   │   │   │   └── DetalleVenta.java
│   │   │   ├── repository/
│   │   │   │   ├── ClienteRepository.java
│   │   │   │   ├── ProductoRepository.java
│   │   │   │   ├── UsuarioRepository.java
│   │   │   │   ├── ConfiguracionRepository.java
│   │   │   │   ├── VentaRepository.java
│   │   │   │   └── jdbc/
│   │   │   │       ├── ClienteJdbcRepository.java
│   │   │   │       ├── ProductoJdbcRepository.java
│   │   │   │       ├── UsuarioJdbcRepository.java
│   │   │   │       ├── ConfiguracionJdbcRepository.java
│   │   │   │       └── VentaJdbcRepository.java
│   │   │   ├── security/
│   │   │   │   ├── PasswordEncoder.java
│   │   │   │   └── Sha256PasswordEncoder.java
│   │   │   ├── service/
│   │   │   │   ├── AutenticacionService.java
│   │   │   │   ├── ClienteService.java
│   │   │   │   ├── ProductoService.java
│   │   │   │   ├── UsuarioService.java
│   │   │   │   ├── ConfiguracionService.java
│   │   │   │   ├── VentaService.java
│   │   │   │   └── ReporteFacturaService.java
│   │   │   ├── session/
│   │   │   │   └── SesionActiva.java
│   │   │   ├── view/
│   │   │   │   ├── LoginView.java
│   │   │   │   ├── MenuPrincipalView.java
│   │   │   │   ├── ClienteView.java
│   │   │   │   ├── ProductoView.java
│   │   │   │   ├── UsuarioView.java
│   │   │   │   ├── VentaView.java
│   │   │   │   ├── HistorialVentasView.java
│   │   │   │   ├── ConfiguracionView.java
│   │   │   │   └── style/EstiloUI.java
│   │   │   └── Main.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── Reportes/
│   └── test/java/ec/edu/uta/ventas/
│       ├── security/
│       ├── service/
│       └── session/
├── .gitignore
├── pom.xml
└── README.md
```

---

## 7. Estrategia de refactorización

La migración se realizó progresivamente para conservar el comportamiento del sistema y poder verificar cada módulo antes de continuar.

| Etapa | Trabajo realizado | Estado |
|---:|---|:---:|
| 1 | Creación del proyecto Maven y estructura base. | ✅ |
| 2 | Configuración y abstracción de PostgreSQL. | ✅ |
| 3 | Refactorización de Clientes. | ✅ |
| 4 | Refactorización de Productos. | ✅ |
| 5 | Refactorización de Usuarios. | ✅ |
| 6 | Autenticación, sesión y configuración. | ✅ |
| 7 | Ventas, historial y facturación. | ✅ |
| 8 | Unificación visual de las vistas Swing. | ✅ |
| 9 | Pruebas unitarias y comprobaciones finales. | ✅ |

---

## 8. Refactorización por módulos

### 8.1 Configuración y acceso a PostgreSQL

Se separó la carga de propiedades de la obtención de conexiones:

```text
application.properties
        ↓
DatabaseConfig
        ↓
PostgresConnectionProvider
        ↑
ConnectionProvider
```

`DatabaseConfig` carga los parámetros de configuración y `PostgresConnectionProvider` proporciona conexiones JDBC. Los repositorios dependen de `ConnectionProvider`, no de una conexión global o estática.

**SOLID:** SRP, DIP e ISP.  
**STUPID reducido:** Tight Coupling y Untestability.

### 8.2 Clientes

Componentes:

- `Cliente`
- `ClienteRepository`
- `ClienteJdbcRepository`
- `ClienteService`
- `ClienteView`

Funcionalidades conservadas:

- Listado y búsqueda dinámica.
- Registro y edición.
- Desactivación lógica y reactivación.
- Consulta de clientes activos e inactivos.

Las validaciones de cédula, teléfono, nombre, apellido, duplicados e identificadores se concentran en `ClienteService`.

La vista no conoce JDBC ni PostgreSQL y `JOptionPane` permanece únicamente en presentación.

**SOLID:** SRP, DIP e ISP.  
**STUPID reducido:** Tight Coupling, Untestability y Duplication.

### 8.3 Productos

Componentes:

- `Producto`
- `ProductoRepository`
- `ProductoJdbcRepository`
- `ProductoService`
- `ProductoView`

Funcionalidades conservadas:

- Listado de activos e inactivos.
- Búsqueda por código, código de barras o nombre.
- Generación automática del código.
- Registro y actualización.
- Desactivación y reactivación lógica.
- Código de barras opcional y control de duplicados.
- Control de stock y resaltado según `STOCK_MINIMO`.

Las reglas de nombre, precio, stock, identificadores y código de barras se encuentran en `ProductoService`. `ProductoJdbcRepository` reutiliza métodos internos para mapeo, consultas y cambios de estado.

**SOLID:** SRP, DIP e ISP.  
**STUPID reducido:** Tight Coupling, Untestability y Duplication.

### 8.4 Usuarios

Componentes:

- `Usuario`
- `UsuarioRepository`
- `UsuarioJdbcRepository`
- `UsuarioService`
- `UsuarioView`

Funcionalidades:

- Listar y buscar usuarios.
- Registrar usuarios.
- Generar username.
- Editar nombre, apellido y rol.
- Desactivar usuarios lógicamente.
- Cambiar contraseña.
- Roles `ADMIN` y `CAJERO`.
- Protección del administrador principal.
- Control de usernames duplicados.

| Componente / función | Estado |
|---|:---:|
| Modelo `Usuario` | ✅ Completado |
| `UsuarioRepository` | ✅ Completado |
| `UsuarioJdbcRepository` | ✅ Completado |
| `UsuarioService` | ✅ Completado |
| `UsuarioView` | ✅ Completado |
| Búsqueda dinámica | ✅ Completado |
| Registro y edición | ✅ Completado |
| Desactivación lógica | ✅ Completado |
| Cambio de contraseña | ✅ Completado |
| Pruebas unitarias | ✅ Completado |

**SOLID:** SRP y DIP.  
**STUPID reducido:** Tight Coupling, Untestability y Duplication.

### 8.5 Autenticación y sesión

El flujo quedó separado de la interfaz:

```text
LoginView
   ↓
AutenticacionService
   ↓
UsuarioRepository
   ↑
UsuarioJdbcRepository
   ↓
PostgreSQL
```

`SesionActiva` mantiene la información del usuario autenticado y permite aplicar permisos según `ADMIN` o `CAJERO`.

Se creó `PasswordEncoder` para evitar que `UsuarioService` y `AutenticacionService` dependan directamente de `Sha256PasswordEncoder`.

```text
PasswordEncoder
      ↑
Sha256PasswordEncoder
```

Esto permite cambiar posteriormente el algoritmo sin modificar la lógica de los servicios.

### 8.6 Configuración del sistema

```text
ConfiguracionView
       ↓
ConfiguracionService
       ↓
ConfiguracionRepository
       ↑
ConfiguracionJdbcRepository
       ↓
PostgreSQL
```

Se administran parámetros como:

- IVA.
- `STOCK_MINIMO`.
- Nombre de la empresa.
- RUC.
- Dirección.

`STOCK_MINIMO` se obtiene desde el módulo de configuración y deja de depender de la sesión, estableciendo una única fuente de información.

### 8.7 Ventas, historial y facturación

El módulo de ventas se reorganizó para separar reglas, persistencia y reportes:

```text
VentaView → VentaService → VentaRepository → VentaJdbcRepository → PostgreSQL
```

Para facturación:

```text
View → ReporteFacturaService → JasperReports
```

`VentaService` se encarga de:

- Validar la venta.
- Obtener el usuario autenticado.
- Consultar el IVA configurado.
- Calcular subtotal, IVA y total.
- Registrar y consultar ventas.
- Consultar detalles.
- Gestionar anulaciones.

`VentaJdbcRepository` registra la venta dentro de una transacción. En ella se registra la cabecera, se comprueba y bloquea el stock, se guardan detalles, se actualizan existencias y se confirma mediante `commit`. Ante un error se ejecuta `rollback`.

La anulación también utiliza una transacción, restaura el stock y marca la venta como anulada. La autorización se basa en el rol `ADMIN`; se eliminó el PIN fijo utilizado por la implementación anterior.

`HistorialVentasView` permite consultar, filtrar, visualizar detalles, anular cuando existen permisos y reimprimir facturas.

`ReporteFacturaService` encapsula JasperReports y se reutiliza tanto después de registrar una venta como para reimpresiones, reduciendo duplicación.

**SOLID:** SRP y DIP.  
**STUPID reducido:** Tight Coupling, Untestability y Duplication.

---

## 9. Unificación de la interfaz gráfica

Se creó `view/style/EstiloUI.java` para reutilizar configuración visual de:

- Botones.
- Campos.
- Tablas.
- Paneles.
- Títulos.
- Colores y otros elementos comunes.

Se aplica en las principales vistas del sistema y reduce la duplicación de configuración Swing sin introducir un framework visual adicional.

---

## 10. Pruebas

Las pruebas unitarias utilizan **JUnit 5** y, cuando corresponde, implementaciones falsas de los repositorios para comprobar la lógica sin depender de PostgreSQL.

Entre los componentes comprobados se encuentran:

- `ClienteService`.
- `ProductoService`.
- `UsuarioService`.
- `AutenticacionService`.
- Configuración.
- Sesión.
- Ventas.
- Seguridad.

Ejemplo de ejecución:

```bash
mvn clean test
```

Resultado final esperado/verificado durante la refactorización:

```text
BUILD SUCCESS
```

También se realizaron pruebas manuales de autenticación, permisos, clientes, productos, usuarios, configuración, ventas, stock, concurrencia, historial, anulaciones y generación/reimpresión de facturas.

---

## 11. Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje principal. |
| Java Swing | Interfaz gráfica. |
| Maven | Gestión de dependencias y construcción. |
| PostgreSQL | Base de datos. |
| JDBC | Persistencia. |
| JUnit 5 | Pruebas unitarias. |
| JasperReports | Generación de facturas. |
| Groovy | Compilación de expresiones del JRXML utilizado. |
| Git / GitHub | Control de versiones y repositorio. |

---

## 12. Requisitos

Antes de ejecutar el proyecto se requiere:

- JDK 21.
- Maven 3.9 o superior.
- PostgreSQL.
- Git, si se desea clonar el repositorio.

Versiones utilizadas durante el desarrollo:

```text
Java:   21.0.7
Maven:  3.9.16
Sistema operativo: Windows 11
```

---

## 13. Configuración y ejecución

### 13.1 Clonar el repositorio

```bash
git clone <URL_DEL_REPOSITORIO>
cd sistema-ventas-refactor
```

### 13.2 Crear la base de datos

Ejecutar el script SQL incluido en el proyecto para crear las tablas y datos mínimos necesarios.

> No se deben publicar credenciales personales ni datos reales de producción en el repositorio.

### 13.3 Configurar PostgreSQL

Configurar `src/main/resources/application.properties` con los parámetros requeridos por el entorno local.

Ejemplo conceptual:

```properties
db.url=jdbc:postgresql://localhost:5432/nombre_base
db.username=usuario_local
db.password=clave_local
```

### 13.4 Compilar y probar

```bash
mvn clean test
```

### 13.5 Ejecutar

```bash
mvn exec:java
```

---

## 14. Permisos por rol

| Módulo | ADMIN | CAJERO |
|---|:---:|:---:|
| Productos | ✅ | — |
| Clientes | ✅ | ✅ |
| Usuarios | ✅ | — |
| Ventas | ✅ | ✅ |
| Configuración | ✅ | — |
| Sistema / sesión | ✅ | ✅ |

---

## 15. Estado final

| Área | Estado |
|---|:---:|
| Estructura Maven | ✅ |
| Configuración PostgreSQL | ✅ |
| Clientes | ✅ |
| Productos | ✅ |
| Usuarios | ✅ |
| Autenticación | ✅ |
| Sesión y permisos | ✅ |
| Configuración | ✅ |
| Ventas | ✅ |
| Historial | ✅ |
| Anulación de ventas | ✅ |
| Control de stock/concurrencia | ✅ |
| Facturación JasperReports | ✅ |
| Estilo visual compartido | ✅ |
| Pruebas unitarias | ✅ |

---

## 16. Deuda técnica y mejoras futuras

La principal deuda técnica identificada es el uso de **SHA-256** para las contraseñas, conservado temporalmente por compatibilidad con los usuarios existentes.

La creación de la interfaz `PasswordEncoder` deja preparado el diseño para sustituir `Sha256PasswordEncoder` por un algoritmo adaptativo específico para contraseñas, como BCrypt o Argon2, sin modificar los servicios que dependen del contrato.

Otras mejoras futuras posibles:

- Pruebas de integración con PostgreSQL.
- Pruebas automatizadas de interfaz Swing.
- Migración controlada del hash de contraseñas.
- Ampliación de validaciones únicamente cuando existan nuevos requisitos del negocio.

---

## 17. Conclusión

La refactorización permitió conservar las funcionalidades principales del Sistema de Ventas y, al mismo tiempo, mejorar su organización interna.

El cambio más importante fue pasar de una estructura con fuerte dependencia entre **Swing, DAO y PostgreSQL** a una separación explícita entre **View, Service, Repository e implementación JDBC**.

Con ello se redujeron especialmente los antipatrones **Tight Coupling, Untestability y Duplication**, mientras que la composición explícita de dependencias, los nombres descriptivos y la decisión de no introducir infraestructura innecesaria también ayudan a controlar los demás problemas asociados a STUPID.

La aplicación de **SRP, DIP e ISP** proporciona una base más clara para mantener y probar el sistema. OCP y LSP se favorecen mediante contratos sustituibles. El objetivo no fue aplicar patrones por obligación, sino utilizar únicamente las abstracciones que solucionan problemas concretos del proyecto y **evitar sobreingeniería**.
