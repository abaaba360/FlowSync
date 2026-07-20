package edu.ustb.flowsync.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.ustb.flowsync.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
