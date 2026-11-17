PetCare 🐾
Aplicación móvil para el cuidado y seguimiento de mascotas
PetCare es un proyecto que estoy desarrollando como parte de mi
formación académica. La idea nació al pensar en algo que les pasa a
muchos dueños de mascotas: a veces tenemos la información de nuestros
animales en diferentes lugares y no siempre es fácil recordar sus
vacunas, citas veterinarias o actividades de cuidado.

Con PetCare quiero reunir esa información en una aplicación Android,
para que sea más sencillo registrar las mascotas, consultar sus datos y
organizar algunos recordatorios desde un mismo lugar.
Estado del proyecto: PetCare es un prototipo académico que
continúa en desarrollo. En este repositorio se encuentra el código y
los módulos trabajados hasta esta etapa. Algunas funciones todavía
necesitan ajustes y pruebas.

¿Qué quiero lograr con PetCare?
Mi objetivo principal es desarrollar una aplicación móvil para Android
que permita organizar la información de las mascotas y llevar un
seguimiento de actividades relacionadas con su cuidado.

Para avanzar hacia ese objetivo, trabajé en lo siguiente:
- Crear un flujo para que los usuarios puedan registrarse e iniciar
  sesión.
- Permitir el registro de mascotas con sus datos principales.
- Consultar el listado de mascotas y acceder al perfil de cada una.
- Organizar recordatorios relacionados con citas y cuidados.
- Integrar servicios de Firebase para gestionar el acceso y guardar
  información en la nube.
¿Qué se puede encontrar en la aplicación?
Estos son los módulos que he trabajado y que están representados en el
código del proyecto:

  Módulo                                ¿Qué permite hacer?
  Bienvenida                          Presenta PetCare y permite entrar
                                      al registro o al inicio de sesión.

  Autenticación                       Incluye los flujos de acceso y
                                      registro implementados en el
                                      proyecto.

  Recuperación de contraseña          Permite iniciar el proceso para
                                      recuperar el acceso a la cuenta.

  Inicio                              Sirve como punto de entrada a las
                                      opciones principales.

  Registro de mascotas                Permite ingresar los datos
                                      principales de una mascota.

  Mis mascotas                        Muestra las mascotas registradas.

  Perfil de mascota                   Permite consultar la información
                                      individual de cada mascota.

  Agenda                              Está orientada a organizar citas y
                                      recordatorios de cuidado.
  
La aplicación todavía tiene aspectos por completar. Por eso, que una
opción aparezca en la interfaz no significa necesariamente que todas sus
acciones estén terminadas o que ya se hayan probado en todos los casos.
Herramientas que utilicé

Para desarrollar PetCare estoy utilizando las siguientes tecnologías:
  Tecnología                          ¿Para qué la utilizo?
  
  Kotlin                              Es el lenguaje que utilizo para
                                      programar la lógica de la
                                      aplicación.

  XML                                 Lo utilizo para construir varias de
                                      las interfaces.

  ViewBinding                         Me permite acceder desde Kotlin a
                                      los elementos definidos en los
                                      layouts.

  Firebase Authentication             Se utiliza para gestionar la
                                      autenticación de los usuarios.

  Cloud Firestore                     Se utiliza para almacenar y
                                      consultar información en la nube.

  Gradle                              Administra la configuración y las
                                      dependencias del proyecto.
  
  Git y GitHub                        Me permiten llevar el control de
                                      versiones y mantener el código del
                                      proyecto.

La configuración del proyecto también tiene habilitado Jetpack Compose,
aunque las pantallas principales que he trabajado y revisado utilizan
layouts XML.
¿Cómo está organizado el proyecto?
He separado el código en partes para que cada una tenga una
responsabilidad. Por un lado están las pantallas, que permiten al
usuario interactuar con la aplicación. También están los modelos, que
representan la información, y los repositorios, donde se agrupan las
operaciones relacionadas con los datos.

Esta es una representación general de la estructura:
Usuario
  |
  v
Aplicación Android
  |
  +-- Pantallas / Activities
  |     +-- MainActivity
  |     +-- DashboardActivity
  |     +-- ForgotPasswordActivity
  |     +-- RegisterPetActivity
  |     +-- MyPetsActivity
  |     +-- PetProfileActivity
  |     +-- ScheduleActivity
  |
  +-- Modelos
  |     +-- Pet
  |     +-- Reminder
  |
  +-- Repositorios
        +-- PetRepository
        +-- ReminderRepository
              |
              v
            Firebase
              +-- Firebase Authentication
              +-- Cloud Firestore
¿Qué hace cada parte?
- Activities y pantallas: manejan la navegación y las acciones que
  realiza el usuario.
- Pet y Reminder: son los modelos que representan las mascotas y
  los recordatorios.
- PetRepository: reúne las operaciones relacionadas con los datos
  de las mascotas.
- ReminderRepository: reúne las operaciones relacionadas con los
  recordatorios.
- Firebase: proporciona los servicios de autenticación y
  almacenamiento en la nube.

Esta organización me ayuda a mantener separada la parte visual de las
operaciones con los datos. No estoy presentando el proyecto como una
implementación completa de MVVM, porque en la estructura revisada no se
identificó una capa de ViewModels.
Servicios de Firebase que utiliza PetCare
Firebase Authentication
Utilizo Firebase Authentication para gestionar la autenticación de los
usuarios. En el código se encuentran flujos relacionados con el acceso
mediante correo y contraseña y la recuperación de contraseña.

También encontré un flujo de autenticación anónima cuando se intenta
registrar una mascota sin que exista una sesión iniciada. Es un
comportamiento que debo revisar para que el manejo de usuarios y la
asociación de sus mascotas sean coherentes con el flujo definitivo de la
aplicación.
Cloud Firestore
Cloud Firestore es el servicio que utilizo como base de datos en la
nube. Es una base de datos NoSQL que organiza la información mediante
colecciones y documentos.

En los repositorios del proyecto se construyen rutas organizadas por
usuario. De forma general, la estructura es la siguiente:
users/
  {uid}/
    pets/
      {petId}
    reminders/
      {reminderId}
De esta manera, las mascotas y los recordatorios se organizan bajo el
identificador del usuario (uid).

Es importante aclarar que organizar los documentos por UID no es
suficiente por sí solo para proteger la información. También es
necesario configurar y probar correctamente las reglas de seguridad de
Firestore.
Modelos principales
Pet

Este modelo representa una mascota. Contiene campos para guardar
información como nombre, especie, raza, fecha de nacimiento, sexo, peso,
microchip y observaciones.

Reminder

Este modelo representa una cita o actividad de cuidado. Incluye
información como título, tipo, fecha, hora, mascota relacionada,
ubicación, notas y estado de cumplimiento.
Estructura de carpetas
La parte principal del código Android se encuentra dentro del paquete
com.example.petcare. Esta es una vista resumida de los archivos
principales identificados:
app/
└── src/main/
    ├── java/com/example/petcare/
    │   ├── MainActivity.kt
    │   ├── DashboardActivity.kt
    │   ├── ForgotPasswordActivity.kt
    │   ├── RegisterPetActivity.kt
    │   ├── MyPetsActivity.kt
    │   ├── PetProfileActivity.kt
    │   ├── ScheduleActivity.kt
    │   ├── Pet.kt
    │   ├── Reminder.kt
    │   ├── PetRepository.kt
    │   ├── ReminderRepository.kt
    │   └── ui/
    ├── res/
    │   ├── layout/
    │   ├── drawable/
    │   └── values/
    └── AndroidManifest.xml
La estructura puede cambiar a medida que siga trabajando en el proyecto.
¿Qué necesito para ejecutar PetCare?
Antes de abrir el proyecto, es necesario tener instalado:
- Android Studio.
- Un JDK compatible con la configuración de Gradle del proyecto.
- Android SDK requerido por la configuración actual.
- Un emulador Android o un dispositivo físico.
- Conexión a Internet.
- Un proyecto de Firebase configurado para la aplicación.
En la configuración que revisé se encontraban minSdk 24 y
compileSdk 37. Es recomendable confirmar estos valores en
app/build.gradle.kts, porque pueden cambiar con nuevas modificaciones.
Pasos para descargar y ejecutar el proyecto
1. Clonar el repositorio desde una terminal:
   git clone https://github.com/OscarAvendano/PetCare.git
2. Abrir Android Studio y seleccionar la carpeta del proyecto.
3. Esperar a que termine la sincronización de Gradle.
4. Instalar los componentes del SDK que solicite Android Studio.
5. Revisar la configuración de Firebase siguiendo los pasos de la
   siguiente sección.
6. Seleccionar un emulador o conectar un dispositivo Android.
7. Ejecutar la aplicación desde la configuración app.
También se puede intentar compilar desde la terminal, ubicada en la
carpeta principal del proyecto:
# Windows
gradlew.bat assembleDebug
# macOS o Linux
./gradlew assembleDebug
Si la compilación termina correctamente, el APK de depuración
normalmente queda en esta ruta:
app/build/outputs/apk/debug/app-debug.apk
Configuración de Firebase
Para ejecutar el proyecto con una configuración propia de Firebase, se
deben seguir estos pasos:
1. Entrar a Firebase Console.
2. Crear un proyecto o seleccionar uno existente.
3. Registrar una aplicación Android con el identificador de paquete del
   proyecto. En la versión revisada se identificó
   com.example.petcare, pero conviene confirmar que siga siendo el
   mismo en Gradle y en el Manifest.
4. Descargar el archivo google-services.json.
5. Copiarlo dentro de la carpeta app/, de manera que quede en
   app/google-services.json.
6. Habilitar en Firebase Authentication los métodos de acceso que
   utilice la versión actual de PetCare.
7. Crear o habilitar Cloud Firestore.
8. Configurar las reglas de seguridad para que cada usuario pueda
   acceder únicamente a sus propios datos.
9. Volver a Android Studio, sincronizar Gradle y ejecutar la
   aplicación.
Sobre la seguridad: el archivo google-services.json contiene
configuración de cliente de Firebase; no equivale a una clave privada de
servidor. Aun así, es importante configurar restricciones de API cuando
corresponda y proteger la información mediante reglas de Firestore. No
se deben subir al repositorio claves privadas, tokens ni credenciales de
cuentas de servicio.
Evidencias del proyecto
Quiero utilizar esta sección para mostrar cómo se ve PetCare y dejar
evidencia de los módulos que he desarrollado.

Para mantener las capturas organizadas, recomiendo guardarlas dentro de
docs/evidencias/ con nombres como estos:
docs/
└── evidencias/
    ├── 01-bienvenida.png
    ├── 02-inicio-sesion.png
    ├── 03-registro-mascota.png
    ├── 04-mis-mascotas.png
    ├── 05-perfil-mascota.png
    ├── 06-agenda.png
    ├── 07-crear-recordatorio.png
    └── 08-repositorio-github.png
Cuando las imágenes estén guardadas y subidas a GitHub, se podrán
visualizar directamente desde este documento:
Pantalla de bienvenida

Inicio de sesión

Registro de mascota

Mis mascotas

Perfil de mascota

Agenda

Crear recordatorio

Presentación del proyecto: PetCare en
Canva
Repositorio:
github.com/OscarAvendano/PetCare
Cosas que quiero seguir mejorando
Durante la revisión del código identifiqué algunos puntos que quiero
corregir o completar en las siguientes etapas:
- Mejorar el manejo de errores para que, si Firestore no puede guardar
  o consultar información, la aplicación muestre el resultado real de
  la operación.
- Revisar el uso de un identificador de usuario de respaldo cuando no
  hay una sesión autenticada.
- Ajustar el flujo de autenticación anónima y la forma en que se
  relacionan los registros con la cuenta del usuario.
- Revisar y probar las reglas de seguridad de Firestore.
- Implementar un mecanismo de almacenamiento persistente para las
  fotografías de las mascotas.
- Ampliar las pruebas para comprobar el comportamiento de los módulos.
- Terminar de conectar las opciones de la interfaz que todavía tienen
  acciones demostrativas.
Dejo estos puntos documentados porque hacen parte del estado actual del
prototipo y me sirven como guía para continuar mejorándolo.
Autoría
PetCare es un proyecto académico que estoy desarrollando de manera
individual.
- Proyecto: PetCare
- Repositorio:
  OscarAvendano/PetCare
- Plataforma: Android
- Lenguaje: Kotlin
- Servicios Cloud: Firebase Authentication y Cloud Firestore
