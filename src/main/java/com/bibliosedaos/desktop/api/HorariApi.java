package com.bibliosedaos.desktop.api;

import com.bibliosedaos.desktop.model.Horari;
import java.util.List;

/**
 * Interfície per a les operacions d'Horaris (sales/dia/hora).
 *
 * @author Sergio
 * @version 1.0-SNAPSHOT
 * @since 2025
 */
public interface HorariApi {

    /**
     * Llista tots els horaris (indicant estat: 'lliure' o 'reservat').
     *
     * @return llista d'horaris
     * @throws ApiException si hi ha errors de comunicació
     */
    List<Horari> getAllHoraris() throws ApiException;

    /**
     * Crea un nou horari (ADMIN només).
     *
     * @param horari dades de l'horari (sala, dia, hora, estat)
     * @return horari creat
     * @throws ApiException si hi ha errors (duplicats, permisos, etc.)
     */
    Horari createHorari(Horari horari) throws ApiException;
}

