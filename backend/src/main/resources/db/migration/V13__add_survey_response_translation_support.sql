IF OBJECT_ID(N'[dbo].[Response_Detail]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Response_Detail', N'comment_vi') IS NULL
BEGIN
    ALTER TABLE [dbo].[Response_Detail]
    ADD [comment_vi] NVARCHAR(MAX) NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Response_Detail]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Response_Detail', N'comment_en') IS NULL
BEGIN
    ALTER TABLE [dbo].[Response_Detail]
    ADD [comment_en] NVARCHAR(MAX) NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Response_Detail]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Response_Detail', N'source_lang') IS NULL
BEGIN
    ALTER TABLE [dbo].[Response_Detail]
    ADD [source_lang] VARCHAR(10) NULL;
END
GO

IF OBJECT_ID(N'[dbo].[Response_Detail]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Response_Detail', N'is_auto_translated') IS NULL
BEGIN
    ALTER TABLE [dbo].[Response_Detail]
    ADD [is_auto_translated] BIT NOT NULL
        CONSTRAINT [DF_ResponseDetail_IsAutoTranslated] DEFAULT ((0));
END
GO

IF OBJECT_ID(N'[dbo].[Response_Detail]', N'U') IS NOT NULL
   AND COL_LENGTH(N'dbo.Response_Detail', N'model_info') IS NULL
BEGIN
    ALTER TABLE [dbo].[Response_Detail]
    ADD [model_info] VARCHAR(100) NULL
        CONSTRAINT [DF_ResponseDetail_ModelInfo] DEFAULT ('default_model');
END
GO

IF OBJECT_ID(N'[dbo].[Response_Detail]', N'U') IS NOT NULL
   AND NOT EXISTS (
       SELECT 1
       FROM sys.indexes
       WHERE [name] = 'IX_ResponseDetail_Translation_Status'
         AND [object_id] = OBJECT_ID(N'[dbo].[Response_Detail]')
   )
BEGIN
    CREATE INDEX [IX_ResponseDetail_Translation_Status]
    ON [dbo].[Response_Detail] ([is_auto_translated], [source_lang])
    INCLUDE ([comment_vi], [comment_en]);
END
GO
