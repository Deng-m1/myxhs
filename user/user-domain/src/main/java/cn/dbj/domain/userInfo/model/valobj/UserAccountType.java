package cn.dbj.domain.userInfo.model.valobj;


public enum UserAccountType {
        DA_V("00", "大V账户", "Celebrity Account"),
        REGULAR("01", "普通账户", "Regular Account"),
        ZOMBIE("03", "僵尸账户", "Zombie Account"),
        ACTIVE("04", "活跃账户", "Active Account"),
        NEW("05", "新创建用户账户", "Newly Created User Account");

        private final String statusCode;
        private final String chineseDescription;
        private final String englishDescription;

        UserAccountType(String statusCode, String chineseDescription, String englishDescription) {
            this.statusCode = statusCode;
            this.chineseDescription = chineseDescription;
            this.englishDescription = englishDescription;
        }

        public String getStatusCode() {
            return statusCode;
        }

        public String getChineseDescription() {
            return chineseDescription;
        }

        public String getEnglishDescription() {
            return englishDescription;
        }
    }

