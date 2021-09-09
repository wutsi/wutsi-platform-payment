[![](https://github.com/wutsi/wutsi-platform-payment/actions/workflows/master.yml/badge.svg)](https://github.com/wutsi/wutsi-platform-payment/actions/workflows/master.yml)
[![](https://github.com/wutsi/wutsi-platform-payment/actions/workflows/pull_requesst.yml/badge.svg)](https://github.com/wutsi/wutsi-platform-payment/actions/workflows/pull_request.yml)

[![JDK](https://img.shields.io/badge/jdk-11-brightgreen.svg)](https://jdk.java.net/11/)
[![](https://img.shields.io/badge/maven-3.6-brightgreen.svg)](https://maven.apache.org/download.cgi)
![](https://img.shields.io/badge/language-kotlin-blue.svg)

`wutsi-platform-payment` is a library that provide payment API for:
- MTN

## Spring Configuration
| Property | Default Value | Description |
|----------|---------------|-------------|
| wutsi.platform.payment.provider |  | REQUIRED. Type of cache: `mtn` |

## MTN Spring Configuration
| Property | Default Value | Description |
|----------|---------------|-------------|
| wutsi.platform.payment.mtn.enabled | false | `true` to enable MTN payment provider |
| wutsi.platform.payment.mtn.environment |  | REQUIRED. `sandbox` or `production` |
| wutsi.platform.payment.mtn.callback-url |  | REQUIRED. Callback URL |
| wutsi.platform.payment.mtn.collection.subscription-key |  | REQUIRED. Subscription Key of the Collection API |
| wutsi.platform.payment.mtn.collection.user-id |  | Collection User ID. REQUIRED in production environment |
| wutsi.platform.payment.mtn.collection.api-key |  | Collection API Key. REDIURED in production environment |
| wutsi.platform.payment.mtn.disbursement.subscription-key |  | REQUIRED. Subscription Key of the Disbursement API |
| wutsi.platform.payment.mtn.disbursement.user-id |  | Disbursement User ID. REQUIRED in production environment |
| wutsi.platform.payment.mtn.disbursement.api-key |  | Disbursement API Key. REDIURED in production environment |

