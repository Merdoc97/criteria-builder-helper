package com.github.builder.jpa;

import com.github.builder.CriteriaRequest;
import com.github.builder.params.FieldsQuery;
import com.github.builder.params.FieldsQueryWrap;
import org.hibernate.criterion.MatchMode;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import javax.persistence.criteria.*;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SpecificationBuilder {


    public <T> Specification<T> builder(@Valid CriteriaRequest request) {

        Specifications res = Specifications.where((root, query, cb) -> {
            FieldsQuery query1 = request.getConditions().stream().findFirst().get();
            List<FieldsQueryWrap> fieldsQueryWrapList = query1.getSearchCriteria()
                    .stream()
                    .map(o -> new FieldsQueryWrap(query1.getProperty(), o, query1.getCriteriaCondition(), query1.getMatchMode()))
                    .collect(Collectors.toList());
            if (fieldsQueryWrapList.isEmpty()){
                throw new IllegalArgumentException("empty request");
            }
            return toPredicate(root, cb, fieldsQueryWrapList.get(0));
        });
        request.getConditions().stream()
                .skip(1)
                .forEach(query -> {
                    query.getSearchCriteria().forEach(o -> {
                        res.and((root, query1, cb) -> toPredicate(root, cb, new FieldsQueryWrap(query.getProperty(), o, query.getCriteriaCondition(), query.getMatchMode())));
                    });
                });
        return res;
    }

    private Predicate toPredicate(Root root, CriteriaBuilder builder, FieldsQueryWrap fieldsQuery) {
        switch (fieldsQuery.getCriteriaCondition()) {
            case LIKE:
                    return builder.and(builder.like(builder.lower(getFetch(root,fieldsQuery.getProperty())), valueWithMatchMode(fieldsQuery.getMatchMode(), fieldsQuery.getSearchCriteria())));

        }
        throw new IllegalArgumentException("unknown condition for query");
    }

    private Path getFetch(Root root, String property) {
        String[] tmp = property.split("\\.");
//        bug but for mvp it's ok
        if (tmp.length>2){
            Path path=((Path) root.fetch(tmp[0], JoinType.LEFT));
            Arrays.stream(tmp).skip(1).forEach(path::get);
            return path;
        }
        return ((Path) root.fetch(tmp[0], JoinType.LEFT)).get(tmp[1]);


    }

    private String valueWithMatchMode(MatchMode matchMode, Object value) {
        if (Objects.isNull(matchMode)) {
            return "'" + value + "'";
        }
        switch (matchMode) {
            case ANYWHERE:
                return "'%" + value + "%'";
            case END:
                return "'" + value + "%'";
            case EXACT:
                return "'" + value + "'";
            default:
                return "'%" + value + "'";
        }

    }

}
