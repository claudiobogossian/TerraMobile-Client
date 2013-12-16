package br.org.funcate.baurudigital.server.util;

import java.sql.Types;
import org.hibernate.dialect.SQLServerDialect;

public class SQLServerNativeDialect extends SQLServerDialect{

  public SQLServerNativeDialect() {
    super();
    registerColumnType(Types.CHAR, "nchar(1)");
    registerColumnType(Types.VARCHAR, "nvarchar($l)");
    registerColumnType(Types.LONGVARCHAR, "nvarchar($l)");
    registerColumnType(Types.CLOB, "nvarchar(MAX)");
  }

}