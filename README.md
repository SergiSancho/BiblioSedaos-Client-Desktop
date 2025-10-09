# BiblioSedaos - Client d'escriptori

Aquest projecte forma part de l’assignatura **M13 - Projecte de desenvolupament d’aplicacions multiplataforma** del CFGS DAM de l’IOC.

## Descripció de l'aplicació
BiblioSedaos és una aplicació destinada a la gestió de reserves i préstecs de llibres per part dels usuaris d'una biblioteca.  
Els principals usuaris són:  
- **Socis de la biblioteca**, que podran realitzar cerques i reserves de llibres.  
- **Treballadors/administradors**, que podran gestionar els prèstecs i donar d'alta usuaris i llibres.  

L’aplicació es divideix en tres components:  
1. **Aplicació mòbil**: destinada principalment als socis de la biblioteca (donar-se d’alta, reservar llibres, consultar catàleg).  
2. **Aplicació d’escriptori**: destinada fonamentalment als administradors (gestió d’usuaris, llibres i prèstecs).  
3. **Servidor**: respon a les peticions de les aplicacions client i accedeix a la base de dades.  

En aquesta part del projecte, es desenvoluparà el **client d’escriptori amb JavaFX**.

## Enllaços als altres components
- [Servidor](https://github.com/dagaro22/biblioSedaosDavid)  
- [Client mòbil](https://github.com/oscar/Mobile-BiblioSedaos)

## Requisits principals
- Gestió d’usuaris (administrador i usuari final)  
- Gestió de llibres (CRUD d’autors i llibres)  
- Gestió d’exemplars (control de l’estat: disponible, prestat, reservat)  
- Préstecs i devolucions (creació i venciments)   
- Estadístiques d’usuari  
- Seguretat (comunicació xifrada i hash de contrasenyes)  

## Eines utilitzades
| Eina | Observacions |
|------|--------------|
| Taiga | Gestió del projecte amb metodologia Scrum |
| GitHub | Control de versions i repositori compartit |
| Java | Llenguatge principal per servidor i client escriptori |
| JavaFX | Interfície gràfica del client d’escriptori |
| PostgreSQL | Base de dades del servidor |
| Kotlin + Jetpack Compose | Llenguatge aplicació mòbil |
| Android Studio | Entorn per aplicació mòbil |
| Spring Boot | Opció per implementar servidor i serveis REST |
| Api REST | Comunicació entre client i servidor |


