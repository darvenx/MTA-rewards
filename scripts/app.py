try:
    print("SCRIPT STARTED", flush=True)

    import snowflake.connector
    from snowflake.connector.pandas_tools import write_pandas
    import pandas as pd
    from sqlalchemy import create_engine

    print("Imports successful", flush=True)

    # ==============================
    # 1️⃣ CONNECT TO MYSQL
    # ==============================
    print("Connecting to MySQL...", flush=True)

    mysql_engine = create_engine(
        "mysql+pymysql://root:Root123$@localhost:3306/moneytransferdb"
    )

    print("Connected to MySQL", flush=True)

    # ==============================
    # 2️⃣ CONNECT TO SNOWFLAKE
    # ==============================
    print("Connecting to Snowflake...", flush=True)

    sf_conn = snowflake.connector.connect(
        user='HYDRID',
        password='Moneytransfer123',
        account='FIWVIJY-GH21909',
        database='MONEYTRANSFERDB',
        schema='PUBLIC',
        role='ACCOUNTADMIN'
    )

    print("Connected to Snowflake", flush=True)

    cursor = sf_conn.cursor()
    cursor.execute("USE DATABASE MONEYTRANSFERDB")
    cursor.execute("USE SCHEMA PUBLIC")
    cursor.close()

    # ==============================
    # 3️⃣ REUSABLE SYNC FUNCTION
    # ==============================
    def sync_table(table_name):
        print(f"\nSyncing table: {table_name}", flush=True)

        query = f"SELECT * FROM {table_name}"

        df = pd.read_sql(query, mysql_engine)

        print(f"Rows fetched: {len(df)}", flush=True)

        if df.empty:
            print(f"{table_name} is empty — skipping.", flush=True)
            return

        # Snowflake uppercase convention
        df.columns = [col.upper() for col in df.columns]

        success, nchunks, nrows, _ = write_pandas(
            conn=sf_conn,
            df=df,
            table_name=table_name.upper(),
            auto_create_table=False,
            overwrite=False
        )

        print(f"Upload success: {success}", flush=True)
        print(f"Rows inserted: {nrows}", flush=True)

    # ==============================
    # 4️⃣ SYNC ALL TABLES
    # ==============================
    tables = ["users", "accounts", "transactions"]

    for table in tables:
        sync_table(table)

    sf_conn.close()
    print("\nALL TABLES SYNCED SUCCESSFULLY", flush=True)

except Exception as e:
    print("CRITICAL ERROR:", e, flush=True)
