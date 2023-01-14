package com.tll.jooqbind;

import java.net.InetAddress;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Types;
import java.util.Objects;

import org.jooq.Binding;
import org.jooq.BindingGetResultSetContext;
import org.jooq.BindingGetSQLInputContext;
import org.jooq.BindingGetStatementContext;
import org.jooq.BindingRegisterContext;
import org.jooq.BindingSQLContext;
import org.jooq.BindingSetSQLOutputContext;
import org.jooq.BindingSetStatementContext;
import org.jooq.Converter;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;

public class PostgresInetAddressBinding implements Binding<Object, InetAddress> {
	private static final long serialVersionUID = 4200576357828653073L;

	@Override
	public Converter<Object, InetAddress> converter() {
		return new Converter<Object, InetAddress>() {
			private static final long serialVersionUID = 1L;

			@Override
			public InetAddress from(Object databaseObject) {
				try {
					return databaseObject == null ? null : InetAddress.getByName("" + databaseObject);
				} catch(Exception e) {
					return null;
				}
			}

			@Override
			public Object to(InetAddress userObject) {
				return userObject == null ? null : userObject.getHostAddress();
			}

			@Override
			public Class<Object> fromType() {
				return Object.class;
			}

			@Override
			public Class<InetAddress> toType() {
				return InetAddress.class;
			}
		};
	}

	@Override
	public void sql(BindingSQLContext<InetAddress> ctx) throws SQLException {
		if (ctx.render().paramType() == ParamType.INLINED)
			ctx.render().visit(DSL.inline(ctx.convert(converter()).value())).sql("::inet");
		else
			ctx.render().sql("?::inet");
	}

	@Override
	public void register(BindingRegisterContext<InetAddress> ctx) throws SQLException {
		ctx.statement().registerOutParameter(ctx.index(), Types.VARCHAR);
	}

	@Override
	public void set(BindingSetStatementContext<InetAddress> ctx) throws SQLException {
		ctx.statement().setString(ctx.index(), Objects.toString(ctx.convert(converter()).value(), null));
	}

	@Override
	public void set(BindingSetSQLOutputContext<InetAddress> ctx) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public void get(BindingGetResultSetContext<InetAddress> ctx) throws SQLException {
		ctx.convert(converter()).value(ctx.resultSet().getString(ctx.index()));
	}

	@Override
	public void get(BindingGetStatementContext<InetAddress> ctx) throws SQLException {
		ctx.convert(converter()).value(ctx.statement().getString(ctx.index()));
	}

	@Override
	public void get(BindingGetSQLInputContext<InetAddress> ctx) throws SQLException {
		throw new SQLFeatureNotSupportedException();
	}

}