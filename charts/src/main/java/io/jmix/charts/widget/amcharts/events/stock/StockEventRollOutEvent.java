/*
 * Copyright 2021 Haulmont.
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

package io.jmix.charts.widget.amcharts.events.stock;


import io.jmix.charts.widget.amcharts.JmixAmStockChartScene;

import java.util.Date;

public class StockEventRollOutEvent extends AbstractStockEventEvent {
    private static final long serialVersionUID = 137246431386490989L;

    public StockEventRollOutEvent(JmixAmStockChartScene scene, String graphId, Date date, String stockEventId) {
        super(scene, graphId, date, stockEventId);
    }
}