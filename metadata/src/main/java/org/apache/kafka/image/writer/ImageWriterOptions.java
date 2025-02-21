/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.kafka.image.writer;

import org.apache.kafka.image.MetadataImage;
import org.apache.kafka.server.common.MetadataVersion;

import java.util.function.Consumer;


/**
 * The options to use when writing an image.
 */
public final class ImageWriterOptions {
    public static class Builder {
        private MetadataVersion metadataVersion;
        private MetadataVersion requestedMetadataVersion;
        private boolean isEligibleLeaderReplicasEnabled = false;
        private Consumer<UnwritableMetadataException> lossHandler = e -> {
            throw e;
        };

        public Builder() {
            this.metadataVersion = MetadataVersion.latestProduction();
        }

        public Builder(MetadataImage image) {
            this.metadataVersion = image.features().metadataVersion();
            this.isEligibleLeaderReplicasEnabled = image.features().isElrEnabled();
        }

        public Builder setMetadataVersion(MetadataVersion metadataVersion) {
            this.requestedMetadataVersion = metadataVersion;
            if (metadataVersion.isLessThan(MetadataVersion.MINIMUM_BOOTSTRAP_VERSION)) {
                // When writing an image, all versions less than 3.3-IV0 are treated as 3.0-IV1.
                // This is because those versions don't support FeatureLevelRecord.
                this.metadataVersion = MetadataVersion.MINIMUM_KRAFT_VERSION;
            } else {
                this.metadataVersion = metadataVersion;
            }
            return this;
        }

        public Builder setEligibleLeaderReplicasEnabled(boolean isEligibleLeaderReplicasEnabled) {
            this.isEligibleLeaderReplicasEnabled = isEligibleLeaderReplicasEnabled;
            return this;
        }

        public MetadataVersion metadataVersion() {
            return metadataVersion;
        }

        public MetadataVersion requestedMetadataVersion() {
            return requestedMetadataVersion;
        }

        public boolean isEligibleLeaderReplicasEnabled() {
            return isEligibleLeaderReplicasEnabled;
        }

        public Builder setLossHandler(Consumer<UnwritableMetadataException> lossHandler) {
            this.lossHandler = lossHandler;
            return this;
        }

        public ImageWriterOptions build() {
            return new ImageWriterOptions(metadataVersion, lossHandler, requestedMetadataVersion, isEligibleLeaderReplicasEnabled);
        }
    }

    private final MetadataVersion metadataVersion;
    private final MetadataVersion requestedMetadataVersion;
    private final Consumer<UnwritableMetadataException> lossHandler;
    private final boolean isEligibleLeaderReplicasEnabled;

    private ImageWriterOptions(
        MetadataVersion metadataVersion,
        Consumer<UnwritableMetadataException> lossHandler,
        MetadataVersion orgMetadataVersion,
        boolean isEligibleLeaderReplicasEnabled
    ) {
        this.metadataVersion = metadataVersion;
        this.lossHandler = lossHandler;
        this.requestedMetadataVersion = orgMetadataVersion;
        this.isEligibleLeaderReplicasEnabled = isEligibleLeaderReplicasEnabled;
    }

    public MetadataVersion metadataVersion() {
        return metadataVersion;
    }
    public boolean isEligibleLeaderReplicasEnabled() {
        return isEligibleLeaderReplicasEnabled;
    }

    public void handleLoss(String loss) {
        lossHandler.accept(new UnwritableMetadataException(requestedMetadataVersion, loss));
    }
}
