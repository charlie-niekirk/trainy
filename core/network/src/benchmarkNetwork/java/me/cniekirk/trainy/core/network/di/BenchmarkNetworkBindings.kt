package me.cniekirk.trainy.core.network.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

@ContributesTo(AppScope::class)
@BindingContainer
object BenchmarkNetworkBindings {
    @Provides
    @SingleIn(AppScope::class)
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder().addInterceptor(MockRttProxyInterceptor).build()
}

private object MockRttProxyInterceptor : Interceptor {
    private val contentType = "application/json".toMediaType()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val body =
            when {
                request.method == "POST" && path == "/v1/client-tokens" -> CLIENT_TOKEN_JSON
                request.method == "GET" && path == "/v1/stops" -> STOPS_JSON
                request.method == "GET" && path == "/v1/stations" -> STATIONS_JSON
                request.method == "GET" &&
                    path.startsWith("/v1/stations/") &&
                    path != "/v1/stations" -> STATION_DETAILS_JSON
                request.method == "GET" && path.startsWith("/v1/boards/") -> CACHED_DATA_JSON
                request.method == "GET" && path.startsWith("/v1/services/") -> CACHED_DATA_JSON
                request.method == "GET" && path == "/v1/meta" -> META_JSON
                request.method == "GET" && path == "/health/live" -> HEALTH_JSON
                request.method == "GET" && path == "/health/ready" -> HEALTH_JSON
                else -> NOT_FOUND_JSON
            }
        val statusCode = if (body == NOT_FOUND_JSON) 404 else 200

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(statusCode)
            .message(if (statusCode == 200) "OK" else "Not Found")
            .body(body.toResponseBody(contentType))
            .build()
    }
}

private const val CLIENT_TOKEN_JSON =
    """
    {
      "token": "benchmark-token",
      "tokenType": "Bearer",
      "expiresAt": "2099-01-01T00:00:00Z"
    }
"""

private const val STOPS_JSON =
    """
    {
      "data": [
        {
          "namespace": "benchmark",
          "description": "Benchmark Central",
          "shortCode": "BNC",
          "longCode": "BENCHMARK",
          "uniqueIdentity": "benchmark-central"
        }
      ],
      "meta": {
        "cacheStatus": "HIT",
        "count": 1
      }
    }
"""

private const val STATIONS_JSON =
    """
    {
      "data": [
        {
          "crsCode": "BNC",
          "name": "Benchmark Central"
        }
      ],
      "meta": {
        "cacheStatus": "HIT",
        "count": 1
      }
    }
    """

private const val STATION_DETAILS_JSON =
    """
    {
      "data": {
        "name": "Benchmark Central",
        "slug": "benchmark-central",
        "sixteenCharacterName": "Benchmark Ctrl",
        "crsCode": "BNC",
        "nationalLocationCode": "0000",
        "minimumConnectionTime": 5,
        "address": {
          "addressLine1": "1 Benchmark Way",
          "addressLine2": "Benchmark",
          "addressLine3": null,
          "addressLine4": null,
          "addressLine5": null,
          "postcode": "BN1 1AA"
        },
        "location": {
          "latitude": 51.5,
          "longitude": -0.1
        },
        "stationAlerts": [],
        "stationOperator": {
          "name": "Benchmark Rail",
          "code": "BN"
        },
        "staffingLevel": "Full Time",
        "toiletsAndChanging": {
          "toilets": {
            "available": true,
            "accessibleToiletsAvailable": true,
            "changingPlacesToiletsAvailable": false
          },
          "showers": {
            "available": false
          }
        },
        "stationAccessibility": {
          "stepFreeCategory": {
            "category": "A",
            "notes": "Step-free access to all platforms."
          },
          "wheelchairsAvailable": true,
          "tactilePaving": "Yes"
        },
        "staffAssistance": {
          "staffHelp": {
            "available": true
          },
          "helpline": {
            "available": true
          }
        },
        "transportLinks": {
          "bus": {
            "available": true,
            "notes": "Local bus services nearby."
          },
          "underground": {
            "available": false
          },
          "airport": {
            "available": false
          },
          "port": {
            "available": false
          },
          "carHire": {
            "available": false
          },
          "taxi": {
            "available": true
          }
        },
        "lifts": {
          "available": true,
          "statement": "Lifts to all platforms."
        },
        "ticketBuying": {
          "ticketOffice": {
            "available": true
          },
          "ticketMachinesAvailable": true,
          "londonFareZone": null
        },
        "loungesAndWaiting": {
          "shelteredWaitingAvailable": true,
          "waitingFacility": {
            "available": true
          }
        },
        "stationFacilities": {
          "cctvAvailable": true,
          "wifi": {
            "available": true
          },
          "refreshments": {
            "available": true
          },
          "shops": {
            "available": false
          },
          "payPhones": false,
          "atm": {
            "available": true
          },
          "defibrillator": {
            "available": true
          }
        },
        "helpAndSupport": {
          "announcements": "PA system",
          "staffHelp": {
            "available": true
          }
        },
        "platformFacilities": {
          "numberOfPlatforms": 4,
          "platforms": [
            {
              "name": "1",
              "waitingType": "Sheltered"
            }
          ]
        },
        "cycling": {
          "cycleStorageAvailable": true,
          "spaces": {
            "numberOfSpaces": 20
          },
          "sheltered": true,
          "cctv": true
        },
        "dropOffPickUp": {
          "available": true,
          "location": "Station forecourt"
        },
        "carParks": {
          "parkingSpacesAvailable": true,
          "numberOfSpaces": 100,
          "accessibleParkingSpacesAvailable": true,
          "numberOfAccessibleSpaces": 6,
          "carParks": [
            {
              "name": "Main car park",
              "numberOfSpaces": 100,
              "cctv": true,
              "freeParking": false,
              "charges": {
                "dailyRate": "8.00"
              }
            }
          ]
        }
      },
      "meta": {
        "cacheStatus": "HIT"
      }
    }
    """

private const val CACHED_DATA_JSON =
    """
    {
      "data": {},
      "meta": {
        "cacheStatus": "HIT"
      }
    }
"""

private const val META_JSON =
    """
    {
      "data": {
        "apiVersion": "benchmark",
        "rttApiVersion": "benchmark",
        "rttApiInfo": {}
      },
      "meta": {
        "cacheStatus": "HIT"
      }
    }
"""

private const val HEALTH_JSON =
    """
    {
      "status": "ok"
    }
"""

private const val NOT_FOUND_JSON =
    """
    {
      "error": {
        "message": "No benchmark mock is registered for this request."
      }
    }
"""
