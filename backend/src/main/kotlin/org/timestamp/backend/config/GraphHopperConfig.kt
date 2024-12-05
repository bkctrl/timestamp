package org.timestamp.backend.config

import com.graphhopper.GraphHopper
import com.graphhopper.config.CHProfile
import com.graphhopper.config.Profile
import com.graphhopper.util.GHUtility
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.exists

data class RouteResponse(
    val time: Long,
    val distance: Double
)

@Configuration
class GraphHopperConfig {

    @Bean
    fun graphHopperInstance(): GraphHopper {
        val osmFile = System.getenv("OSM_FILE") ?: "ontario-latest.osm.pbf"
        val path = Path("osm/$osmFile")
        val cachePath = Path("graph-cache")

        val hopper = createGraphHopperInstance(path, cachePath)
        return hopper
    }

    companion object {
        private val profiles = listOf(
            Profile("car").apply {
                customModel = GHUtility.loadCustomModelFromJar("car.json")
            },
            Profile("bike").apply {
                customModel = GHUtility.loadCustomModelFromJar("bike.json")
            },
            Profile("foot").apply {
                customModel = GHUtility.loadCustomModelFromJar("foot.json")
            }
        )

        private val chProfiles = listOf(CHProfile("car"), CHProfile("bike"), CHProfile("foot"))
        private const val ENCODED_VALUES = "car_access,car_average_speed," +
                "bike_priority,mtb_rating,hike_rating,bike_access,roundabout,bike_average_speed," +
                "foot_access,foot_priority,foot_average_speed"

        private fun createGraphHopperInstance(path: Path, cachePath: Path): GraphHopper {
            assert(path.exists()) { "OSM file does not exist" }

            val hopper = GraphHopper()

            // Configs for the graphhopper instance
            hopper.osmFile = path.absolute().toString() // set the path to the OSM file
            hopper.setGraphHopperLocation(cachePath.absolute().toString()) // cache path

            hopper.encodedValuesString = ENCODED_VALUES
            hopper.chPreparationHandler.setCHProfiles(chProfiles) // enable speed modes
            hopper.setProfiles(profiles)

            // Load graphhopper cache if it exists, otherwise create it
            hopper.importOrLoad()
            return hopper
        }
    }
}

