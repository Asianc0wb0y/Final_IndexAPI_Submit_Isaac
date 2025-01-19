package de.solactive.challenge.indexapi.mappers;

import de.solactive.challenge.indexapi.dto.*;
import de.solactive.challenge.indexapi.entities.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface IndexMapper {

    @Mapping(target ="shares", expression = "java(convertIndexMembers2shares(indexDTO.getIndexMembers()))")
    IndexEntity toEntity(IndexDTO indexDTO);

    // Helper method to convert indexMembers from IndexDTO to shares in IndexEntity
    default Map<String, ShareEntity> convertIndexMembers2shares(List<ShareDTO> indexMembers) {
        return indexMembers.stream().collect(Collectors.toMap(
                ShareDTO::getShareName, // key for hashmap
                indexMember -> new ShareEntity(
                        indexMember.getShareName(),
                        indexMember.getSharePrice(),
                        indexMember.getNumberOfShares())
        ));


    }


    // Converts IndexEntity to IndexStateResponseDTO (for the method GET /indexState)
    @Mapping(target = "indexValue", expression = "java(calculateTotalIndexValue(indexEntity))")
    @Mapping(target = "indexMembers", expression = "java(mapSharesToResponse(indexEntity))")
    IndexStateResponseDTO toDto(IndexEntity indexEntity);

    // Helper method to convert shares to DTOs with calculated index values and weights
    default List<IndexMemberResponseDTO> mapSharesToResponse(IndexEntity indexEntity) {
        double totalIndexValue = calculateTotalIndexValue(indexEntity);
        return indexEntity.getShares().values().stream()
                .sorted(Comparator.comparing(ShareEntity::getShareName))
                .map(share -> {
                    double shareValue = share.getSharePrice() * share.getNumberOfShares();
                    double weightPct = (shareValue / totalIndexValue) * 100;
                    return new IndexMemberResponseDTO(
                            share.getShareName(),
                            share.getSharePrice(),
                            share.getNumberOfShares(),
                            weightPct,
                            shareValue
                    );
                })
                .collect(Collectors.toList());
    }

    // Helper method to calculate the total index value
    default double calculateTotalIndexValue(IndexEntity indexEntity) {
        return indexEntity.getShares().values().stream()
                .mapToDouble(share -> share.getSharePrice() * share.getNumberOfShares())
                .sum();
    }


}
