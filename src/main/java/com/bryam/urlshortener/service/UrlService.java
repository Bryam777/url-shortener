package com.bryam.urlshortener.service;

import java.util.List;

import com.bryam.urlshortener.dto.request.ShortenUrlRequestDTO;
import com.bryam.urlshortener.dto.response.ShortenUrlResponseDTO;
import com.bryam.urlshortener.model.Url;

import jakarta.servlet.http.HttpServletRequest;

public interface UrlService {

  //Acortar una url para un usuario anónimo
  //Es con código hash y reutilizara las urls existentes de otros usuarios anónimos
  ShortenUrlResponseDTO shortenAnonymousUrl(ShortenUrlRequestDTO requestDTO, HttpServletRequest httpServletResponse);

  //Acortar una url para un usuario registrado
  //Usa base62 con slug o dominio personal al acortar la url
  ShortenUrlResponseDTO shortenRegisteredUrl(ShortenUrlRequestDTO requestDTO, Long userId);

  //Busca por el shortCode para preparar la redireccionamiento
  //Valida el estado y actualiza el contador de los clicks
  Url getUrlForRedirection(String shortCode);

  //Obtiene todas las urls de un usuario registrado
  List<ShortenUrlResponseDTO> getUserUrls(Long userId);

  //Eliminar una Url de un usuario registrado
  void deleteUrl(String shortCode, Long userId);

  //Actualizar el redireccionamiento de una url corta, ingresando otro destino o Url
  void updateDestinationUrl(String shortCode, String newUrl, Long userId);

}
