/*
 * Copyright 2018 Alexandre Roman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.alexandreroman.wifiscanner.hosts

import java.net.InetAddress

/**
 * Service used to ping hosts on a network.
 * @author Alexandre Roman
 */
object HostPinger {
    /**
     * Ping a host. This method is blocking as network requests are sent:
     * do not call this method on the UI thread.
     * @return true if the host responds to the ping request
     */
    fun pingHost(hostAddress: InetAddress): Boolean {
        return hostAddress.isReachable(4000)
    }
}
