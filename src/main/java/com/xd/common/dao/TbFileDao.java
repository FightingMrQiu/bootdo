package com.xd.common.dao;

import com.xd.common.domain.TbFileDO;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

/**
 * 配置文件
 * @author andrew
 * @email 1992lcg@163.com
 * @date 2018-08-08 11:24:53
 */
@Mapper
public interface TbFileDao {

	TbFileDO get(Integer id);
	
	List<TbFileDO> list(Map<String, Object> map);
	
	int count(Map<String, Object> map);
	
	int save(TbFileDO tbFile);
	
	int update(TbFileDO tbFile);
	
	int remove(Integer Id);
	
	int batchRemove(Integer[] ids);
}
