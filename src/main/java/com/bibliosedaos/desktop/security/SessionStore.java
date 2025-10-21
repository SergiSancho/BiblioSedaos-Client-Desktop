package com.bibliosedaos.desktop.security;

/**
 * Emmagatzematge de sessió d'usuari amb patró Singleton.
 *
 * Gestiona les dades de sessió de l'usuari autenticat com ara token JWT,
 * identificador d'usuari, rol i dades personals. Les dades s'emmagatzemen
 * només en memòria i es netegen en fer logout.
 *
 * Assistència d'IA: fragment(s) de codi generat / proposat / refactoritzat per ChatGPT-5 i DeepSeek.
 * S'ha revisat i adaptat manualment per l'autor. Veure llegeixme.pdf per detalls.
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public class SessionStore {
    private static final SessionStore INSTANCE = new SessionStore();

    private String token;
    private String userId;
    private int rol;
    private String nom;
    private String cognom1;
    private String cognom2;

    /**
     * Constructor privat per a patró Singleton.
     */
    private SessionStore() {}

    /**
     * Retorna la instància única del SessionStore.
     *
     * @return Instància singleton de SessionStore
     */
    public static SessionStore getInstance() { return INSTANCE; }

    /**
     * Estableix el token d'accés JWT.
     *
     * @param token Token JWT d'autenticació
     */
    public synchronized void setToken(String token) { this.token = token; }

    /**
     * Retorna el token d'accés JWT.
     *
     * @return Token JWT d'autenticació
     */
    public synchronized String getToken() { return token; }

    /**
     * Estableix l'identificador d'usuari.
     *
     * @param userId Identificador únic de l'usuari
     */
    public synchronized void setUserId(String userId) { this.userId = userId; }

    /**
     * Retorna l'identificador d'usuari.
     *
     * @return Identificador únic de l'usuari
     */
    public synchronized String getUserId() { return userId; }

    /**
     * Estableix el rol de l'usuari.
     *
     * @param rol Rol de l'usuari (2=admin, 1=usuari)
     */
    public synchronized void setRol(int rol) { this.rol = rol; }

    /**
     * Retorna el rol de l'usuari.
     *
     * @return Rol de l'usuari
     */
    public synchronized int getRol() { return rol; }

    /**
     * Estableix el nom de l'usuari.
     *
     * @param nom Nom de l'usuari
     */
    public synchronized void setNom(String nom) { this.nom = nom; }

    /**
     * Retorna el nom de l'usuari.
     *
     * @return Nom de l'usuari
     */
    public synchronized String getNom() { return nom; }

    /**
     * Estableix el primer cognom de l'usuari.
     *
     * @param cognom1 Primer cognom de l'usuari
     */
    public synchronized void setCognom1(String cognom1) { this.cognom1 = cognom1; }

    /**
     * Retorna el primer cognom de l'usuari.
     *
     * @return Primer cognom de l'usuari
     */
    public synchronized String getCognom1() { return cognom1; }

    /**
     * Estableix el segon cognom de l'usuari.
     *
     * @param cognom2 Segon cognom de l'usuari
     */
    public synchronized void setCognom2(String cognom2) { this.cognom2 = cognom2; }

    /**
     * Retorna el segon cognom de l'usuari.
     *
     * @return Segon cognom de l'usuari
     */
    public synchronized String getCognom2() { return cognom2; }

    /**
     * Neteja totes les dades de sessió.
     * S'executa en fer logout.
     */
    public synchronized void clear() {
        token = null;
        userId = null;
        rol = 1;
        nom = null;
        cognom1 = null;
        cognom2 = null;
    }
}

