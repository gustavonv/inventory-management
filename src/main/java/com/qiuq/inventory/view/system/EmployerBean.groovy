/**
 *
 */
package com.qiuq.inventory.view.system

import java.lang.reflect.Field

import javax.persistence.Column
import javax.persistence.Transient

import org.primefaces.model.TreeNode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Scope
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import org.springframework.util.MultiValueMap
import org.springframework.util.ReflectionUtils
import org.springframework.util.ReflectionUtils.FieldCallback
import org.springframework.util.ReflectionUtils.FieldFilter

import com.qiuq.inventory.bean.config.GridColumnDefinition
import com.qiuq.inventory.bean.system.Department
import com.qiuq.inventory.bean.system.Employer
import com.qiuq.inventory.repository.config.GridColumnDefinitionRepo
import com.qiuq.inventory.repository.system.EmployerRepo
import com.qiuq.inventory.service.system.DepartmentService
import com.qiuq.inventory.view.CommonBeanDataModel

/**
 * @author qiushaohua 2012-9-13
 * @version 0.0.1
 *
 */
@Component
@Scope("request")
class EmployerBean {
    static final int MODAL_ID = 45;

    TreeNode selectedTreeNode;

    String query;

    boolean isHideDisabled;

    CommonBeanDataModel employers;

    List<GridColumnDefinition> columns;

    String sort;

    @Autowired
    EmployerRepo employerRepo;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    GridColumnDefinitionRepo gridColumnDefinitionRepo;

    CommonBeanDataModel getEmployers(){
        if(employers == null){
            employers = initEmployers();
        }
        return employers;
    }

    /**
     * change the setter to private
     *
     * @param employers
     * @author qiushaohua 2012-9-17
     */
    private void setEmployers(CommonBeanDataModel employers){
    }

    /**
     *
     * @return
     * @author qiushaohua 2012-9-17
     */
    CommonBeanDataModel initEmployers(){
        if(query == null || query.trim() == ""){
            query = "";
        }

        int selectedDepartmentId = 0;
        if(selectedTreeNode != null){
            Department selectedDepartment = selectedTreeNode.getData();
            if(selectedDepartment != null){
                selectedDepartmentId = selectedDepartment.id;
            }
        }
        MultiValueMap<Integer, Integer> sub = departmentService.getSublist();
        List<Integer> departmentIds = sub.get(selectedDepartmentId);

        return new CommonBeanDataModel(employerRepo.findByDepartmentAndQuery(departmentIds, "%${query}%", parseSort()));
    }

    private Sort parseSort(){
        if(sort == null){
            return new Sort("id");
        }

        String[] arr = sort.split(",");
        if(arr.length == 1){
            return new Sort(arr[0]);
        }
        if(arr.length == 2){
            if(arr[1] == "asc"){
                return new Sort(Sort.Direction.ASC, arr[0]);
            }else{
                return new Sort(Sort.Direction.DESC, arr[0]);
            }
        }
        return new Sort("id");
    }

    /**
     *
     * @return
     * @author qiushaohua 2012-9-17
     */
    List<GridColumnDefinition> getColumns(){
        if(columns == null){
            columns = initColumns();
        }
        return columns;
    }


    /**
     * change the setter to private
     *
     * @param employers
     * @author qiushaohua 2012-9-17
     */
    private void setColumns(List<GridColumnDefinition> Columns){
    }

    /**
     *
     * @return
     * @author qiushaohua 2012-9-17
     */
    private List<GridColumnDefinition> initColumns(){
        List<GridColumnDefinition> columns = gridColumnDefinitionRepo.findByModalId(MODAL_ID);

        Map<String, String> mapping = getColumnMapping(Employer);

        columns.each {
            it.fieldName = mapping.get(it.fieldName.toLowerCase());
        }
        return columns;
    }

    /**
     *
     * @param entityClass
     * @return
     * @author qiushaohua 2012-9-16
     */
    protected Map<String, String> getColumnMapping(Class<?> entityClass){
        Map<String, String> mapping = [:];

        def fieldCallback = {
            Field field->
            Column column= field.getAnnotation(Column.class);
            if(column == null || column.name() == ""){
                mapping.put(field.name.toLowerCase(), field.name);
            }else{
                mapping.put(column.name().toLowerCase(), field.name);
            }
        } as FieldCallback;

        def fieldFilter = {
            Field field->
            field.getAnnotation(Transient.class) == null;
        } as FieldFilter;

        ReflectionUtils.doWithFields(entityClass,fieldCallback, fieldFilter);

        return mapping;
    }
}