package br.edu.ifc.bikes.dto.mapper;

import br.edu.ifc.bikes.dto.UsuarioRequestDTO;
import br.edu.ifc.bikes.dto.UsuarioResponseDTO;
import br.edu.ifc.bikes.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    Usuario toUsuario(UsuarioRequestDTO usuarioRequestDTO);

    @Mapping(target = "role", expression = "java(toRoleName(usuario.getRole()))")
    UsuarioResponseDTO toResponse(Usuario usuario);

    List<UsuarioResponseDTO> toResponse(List<Usuario> usuarios);

    default String toRoleName(Usuario.Role role) {
        return role == null ? null : role.name().substring("ROLE_".length());
    }
}