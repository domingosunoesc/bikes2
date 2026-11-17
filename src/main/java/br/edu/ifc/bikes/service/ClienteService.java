package br.edu.ifc.bikes.service;

import br.edu.ifc.bikes.dto.ClienteRequestDTO;
import br.edu.ifc.bikes.dto.ClienteResponseDTO;
import br.edu.ifc.bikes.dto.mapper.ClienteMapper;
import br.edu.ifc.bikes.entity.Cliente;
import br.edu.ifc.bikes.entity.Usuario;
import br.edu.ifc.bikes.exception.ClienteConflictException;
import br.edu.ifc.bikes.exception.EntityNotFoundException;
import br.edu.ifc.bikes.repository.ClienteRepository;
import br.edu.ifc.bikes.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final ClienteMapper clienteMapper;

    public ClienteResponseDTO create(Long usuarioId, ClienteRequestDTO request) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Usuário com id=%d não encontrado", usuarioId))
        );
        String cpf = request.cpf().replaceAll("\\D", "");

        if (clienteRepository.existsByUsuarioId(usuarioId)) {
            throw new ClienteConflictException("Este usuário já possui cadastro de cliente");
        }
        if (clienteRepository.existsByCpf(cpf)) {
            throw new ClienteConflictException("Este CPF já possui cadastro de cliente");
        }

        Cliente cliente = new Cliente();
        cliente.setNomeCompleto(request.nomeCompleto());
        cliente.setCpf(cpf);
        cliente.setUsuario(usuario);

        try {
            return clienteMapper.toResponse(clienteRepository.saveAndFlush(cliente));
        } catch (DataIntegrityViolationException ex) {
            throw new ClienteConflictException("CPF ou usuário já possui cadastro de cliente");
        }
    }

    public ClienteResponseDTO getById(Long id) {
        return clienteMapper.toResponse(clienteRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Cliente com id=%d não encontrado", id))
        ));
    }

    public ClienteResponseDTO getByUsuarioId(Long usuarioId) {
        return clienteMapper.toResponse(clienteRepository.findByUsuarioId(usuarioId).orElseThrow(
                () -> new EntityNotFoundException(
                        String.format("Cadastro de cliente para o usuário id=%d não encontrado", usuarioId))
        ));
    }

    public List<ClienteResponseDTO> getAll() {
        return clienteMapper.toResponse(clienteRepository.findAll());
    }
}
