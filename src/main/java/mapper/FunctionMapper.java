package mapper;

import entity.TabulatedFunctionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface FunctionMapper {
    FunctionMapper INSTANCE = Mappers.getMapper(FunctionMapper.class);

    FunctionListDto toDto(TabulatedFunctionEntity entity);
}
