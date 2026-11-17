package br.edu.ifc.bikes.service;

import br.edu.ifc.bikes.dto.BicicletaRequestDTO;
import br.edu.ifc.bikes.dto.BicicletaResponseDTO;
import br.edu.ifc.bikes.dto.mapper.BicicletaMapper;
import br.edu.ifc.bikes.exception.BicicletaConflictException;
import br.edu.ifc.bikes.exception.EntityNotFoundException;
import br.edu.ifc.bikes.repository.BicicletaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BicicletaService {
    private final BicicletaRepository bicicletaRepository;
    private final BicicletaMapper bicicletaMapper;

    public BicicletaResponseDTO create(BicicletaRequestDTO request) {
        if (bicicletaRepository.existsByCodigo(request.codigo())) {
            throw codigoAlreadyExists(request.codigo());
        }

        try {
            return bicicletaMapper.toResponse(
                    bicicletaRepository.saveAndFlush(bicicletaMapper.toEntity(request)));
        } catch (DataIntegrityViolationException ex) {
            throw codigoAlreadyExists(request.codigo());
        }
    }

    public BicicletaResponseDTO getByCodigo(String codigo) {
        return bicicletaMapper.toResponse(bicicletaRepository.findByCodigo(codigo).orElseThrow(
                () -> new EntityNotFoundException(
                        String.format("Bicicleta com código '%s' não encontrada", codigo))
        ));
    }

    private BicicletaConflictException codigoAlreadyExists(String codigo) {
        return new BicicletaConflictException(
                String.format("Já existe uma bicicleta com o código '%s'", codigo));
    }
}
