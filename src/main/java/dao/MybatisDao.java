package dao;

import meta.Config;
import meta.Deliver;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by yangz on 2017/9/1 10:15.
 */
public interface MybatisDao {
    @Select("select * from deliverList")
    List<Deliver> getDeliverList();

    @Select("select id from deliverList where name = #{name}")
    int getDeliverId (@Param("name") String name);

    @Select("select * from deliverconfig dc left join deliverlist dl on dc.deliverId = dl.id where dl.name = #{deliverName}")
    List<Config> getConfigs(@Param("deliverName") String deliverName);

    @Select("select * from deliverconfig where id = #{configId}")
    Config getConfigById(@Param("configId") int configId);


    @Insert("insert into deliverconfig(deliverId, min, max, unit, price, totalPrice) values (#{deliverId}, #{min}, #{max}, #{unit}, #{price}, #{totalPrice})")
    @Options(useGeneratedKeys = true)
    Integer insertConfig(Config config);

    @Update("update deliverconfig set deliverId = #{deliverId}, min = #{min}, max = #{max}, unit = #{unit}, price = #{price}, totalPrice = #{totalPrice} where id = #{id}")
    void updateDeliverConfig(Config config);

    @Select("SELECT dc.id, dc.deliverId,dc.min,dc.max,dc.unit,dc.price,dc.totalPrice " +
            "FROM deliverarea da LEFT JOIN deliverconfig dc ON da.configId = dc.id " +
            "LEFT JOIN deliverlist dl ON dc.deliverId = dl.id WHERE dl.NAME = #{deliverName} AND da.province = #{province}")
    List<Config> getCalculateConfigs (@Param("deliverName") String deliverName,@Param("province") String province);

    @Delete("delete from deliverconfig where id = #{id}")
    void deleteDeliverConfig(@Param("id") int id);

    @Insert("insert into deliverarea (configId, province) values (#{configId}, #{province})")
    void insertArea(@Param("configId") int configId, @Param("province") String province);

    @Delete("delete from deliverarea where configId = #{configId}")
    void deleteArea(@Param("configId") int configId);

    @Select("select ds.short from deliverarea da right join deliverareashort ds on da.province = ds.name where configId = #{configId}")
    List<String> getShortArea(@Param("configId") int configId);

    @Select("select province from deliverarea where configId = #{configId}")
    List<String> getDeliverArea(@Param("configId") int configId);

    @Select("select standard from deliverstandard where vague = #{vague}")
    String getStandardProvince(@Param("vague") String vague);
}
