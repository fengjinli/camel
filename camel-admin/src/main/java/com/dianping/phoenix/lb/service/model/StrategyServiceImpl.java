/**
 * Project: phoenix-load-balancer
 * <p/>
 * File Created at 2013-10-17
 */
package com.dianping.phoenix.lb.service.model;

import com.dianping.phoenix.lb.constant.MessageID;
import com.dianping.phoenix.lb.dao.StrategyDao;
import com.dianping.phoenix.lb.exception.BizException;
import com.dianping.phoenix.lb.model.entity.Strategy;
import com.dianping.phoenix.lb.service.ConcurrentControlServiceTemplate;
import com.dianping.phoenix.lb.utils.ExceptionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @author Leo Liang
 */
@Service
public class StrategyServiceImpl extends ConcurrentControlServiceTemplate implements StrategyService {

	private StrategyDao strategyDao;

	/**
	 * @param strategyDao
	 */
	@Autowired(required = true)
	public StrategyServiceImpl(StrategyDao strategyDao) {
		super();
		this.strategyDao = strategyDao;
	}

	@PostConstruct
	private void initStrategy() throws BizException {
		List<Strategy> strategies = listStrategies();

		if (strategies.size() == 0) {
			initDefaultStrategy();
		}
	}

	private void initDefaultStrategy() throws BizException {
		Strategy ipHash = new Strategy();

		ipHash.setName("ip-hash");
		ipHash.setType("ip-hash");
		ipHash.setDynamicAttribute("argumentType", "NON_ARGUMENT");

		addStrategy("ip-hash", ipHash);

		Strategy roundRobin = new Strategy();

		roundRobin.setName("round-robin");
		roundRobin.setType("round-robin");

		addStrategy("round-robin", roundRobin);

		Strategy consistentHashRid = new Strategy();

		consistentHashRid.setName("consistent_hash_rid");
		consistentHashRid.setType("consistent_hash");
		consistentHashRid.setDynamicAttribute("target", "$arg_rid");

		addStrategy("consistent_hash_rid", consistentHashRid);

		Strategy consistentHashRequestId = new Strategy();

		consistentHashRequestId.setName("consistent_hash_arg_requestId");
		consistentHashRequestId.setType("consistent_hash");
		consistentHashRequestId.setDynamicAttribute("target", "$arg_requestId");
		consistentHashRequestId.setDynamicAttribute("argumentType", "ONE_ARGUMENT");

		addStrategy("consistent_hash_arg_requestId", consistentHashRequestId);
	}

	/**
	 * @param strategyDao the strategyDao to set
	 */
	public void setStrategyDao(StrategyDao strategyDao) {
		this.strategyDao = strategyDao;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.phoenix.lb.service.StrategyService#listStrategies()
	 */
	@Override
	public List<Strategy> listStrategies() {
		try {
			return read(new ReadOperation<List<Strategy>>() {

				@Override
				public List<Strategy> doRead() throws Exception {
					return strategyDao.list();
				}
			});
		} catch (BizException e) {
			// ignore
			return null;
		}
	}

	@Override
	public Strategy findStrategy(final String strategyName) throws BizException {
		if (StringUtils.isBlank(strategyName)) {
			ExceptionUtils.throwBizException(MessageID.STRATEGY_NAME_EMPTY);
		}

		return read(new ReadOperation<Strategy>() {

			@Override
			public Strategy doRead() throws BizException {
				return strategyDao.find(strategyName);
			}
		});
	}

	@Override
	public void addStrategy(String strategyName, final Strategy strategy) throws BizException {
		if (strategyName == null || strategy == null) {
			return;
		}

		if (!strategyName.equals(strategy.getName())) {
			return;
		}

		validate(strategy);

		write(new WriteOperation<Void>() {

			@Override
			public Void doWrite() throws Exception {
				strategyDao.add(strategy);
				return null;
			}
		});
	}

	@Override
	public void deleteStrategy(final String strategyName) throws BizException {
		if (StringUtils.isBlank(strategyName)) {
			ExceptionUtils.throwBizException(MessageID.STRATEGY_NAME_EMPTY);
		}

		try {
			write(new WriteOperation<Void>() {

				@Override
				public Void doWrite() throws Exception {
					strategyDao.delete(strategyName);
					return null;
				}
			});
		} catch (BizException e) {
			// ignore
		}
	}

	@Override
	public void modifyStrategy(final String strategyName, final Strategy strategy) throws BizException {
		if (strategyName == null || strategy == null) {
			return;
		}

		if (!strategyName.equals(strategy.getName())) {
			return;
		}

		validate(strategy);

		write(new WriteOperation<Void>() {

			@Override
			public Void doWrite() throws Exception {
				strategyDao.update(strategy);
				return null;
			}
		});

	}

	private void validate(Strategy strategy) throws BizException {
		if (StringUtils.isBlank(strategy.getType())) {
			ExceptionUtils.throwBizException(MessageID.STRATEGY_TYPE_EMPTY);
		}
	}
}
