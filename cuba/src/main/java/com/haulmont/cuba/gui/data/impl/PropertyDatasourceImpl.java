/*
 * Copyright 2019 Haulmont.
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
package com.haulmont.cuba.gui.data.impl;

import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.gui.data.*;
import io.jmix.core.DevelopmentException;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanProperty;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.Entity;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;

import javax.annotation.Nullable;
import java.util.*;

public class PropertyDatasourceImpl<T extends Entity>
        extends AbstractDatasource<T>
        implements Datasource<T>, DatasourceImplementation<T>, PropertyDatasource<T> {

    protected Datasource masterDs;
    protected MetaProperty metaProperty;
    protected MetaClass metaClass;
    protected FetchPlan view;

    @Override
    public void setup(String id, Datasource masterDs, String property) {
        this.id = id;
        this.masterDs = masterDs;
        metaProperty = masterDs.getMetaClass().findProperty(property);
        initParentDsListeners();
    }

    @SuppressWarnings("unchecked")
    protected void initParentDsListeners() {
        masterDs.addItemChangeListener(e -> {
            Entity prevValue = getItem(e.getPrevItem());
            Entity newValue = getItem(e.getItem());
            reattachListeners(prevValue, newValue);
            fireItemChanged((T) prevValue);
        });

        masterDs.addStateChangeListener(e ->
                fireStateChanged(e.getPrevState())
        );

        masterDs.addItemPropertyChangeListener(e -> {
            if (e.getProperty().equals(metaProperty.getName()) && !Objects.equals(e.getPrevValue(), e.getValue())) {
                reattachListeners((Entity) e.getPrevValue(), (Entity) e.getValue());
                fireItemChanged((T) e.getPrevValue());
            }
        });
    }

    protected void reattachListeners(Entity prevItem, Entity item) {
        if (prevItem != item) {
            detachListener(prevItem);
            attachListener(item);
        }
    }

    @Override
    public State getState() {
        return masterDs.getState();
    }

    @Override
    public T getItem() {
        final Entity item = masterDs.getItem();
        return getItem(item);
    }

    @Override
    @Nullable
    public T getItemIfValid() {
        backgroundWorker.checkUIAccess();

        return getState() == State.VALID ? getItem() : null;
    }

    protected T getItem(Entity item) {
        return item == null ? null : (T) EntityValues.getValue(item, metaProperty.getName());
    }

    @Override
    public MetaClass getMetaClass() {
        if (metaClass == null) {
            MetaClass propertyMetaClass = metaProperty.getRange().asClass();
            metaClass = metadata.getExtendedEntities().getEffectiveMetaClass(propertyMetaClass);
        }
        return metaClass;
    }

    @Override
    public FetchPlan getView() {
        if (view == null) {
            MetaClass metaMetaClass = masterDs.getMetaClass();
            if (metadata.getTools().isPersistent(metaMetaClass)
                    || metadata.getTools().isEmbeddable(metaMetaClass)) {
                FetchPlan masterView = masterDs.getView();
                if (masterView == null) {
                    throw new DevelopmentException("No view for datasource " + masterDs.getId(),
                            ParamsMap.of("masterDs", masterDs.getId(),
                                    "propertyDs", getId()));
                }

                FetchPlanProperty property = masterView.getProperty(metaProperty.getName());
                if (property == null) {
                    return null;
                }

                if (property.getFetchPlan() == null) {
                    throw new DevelopmentException(
                            String.format("Invalid view definition: %s. Property '%s' must have a view",
                                    masterView, property),
                            ParamsMap.of("masterDs", masterDs.getId(),
                                    "propertyDs", getId(),
                                    "masterView", masterView,
                                    "property", property)
                    );
                }
                view = metadata.getViewRepository().getView(getMetaClass(), property.getFetchPlan().getName());
                //anonymous (nameless) view
                if (view == null)
                    view = property.getFetchPlan();
            }
        }
        return view;
    }

    @Override
    public DsContext getDsContext() {
        return masterDs.getDsContext();
    }

    @Override
    public DataSupplier getDataSupplier() {
        return masterDs.getDataSupplier();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void commit() {
        if (!allowCommit) {
            return;
        }

        if (getCommitMode() == CommitMode.PARENT) {
            if (parentDs == null) {
                throw new IllegalStateException("parentDs is null while commitMode=PARENT");
            }

            if (parentDs instanceof CollectionDatasource) {
                CollectionDatasource parentCollectionDs = (CollectionDatasource) parentDs;
                for (Entity item : itemsToCreate) {
                    if (parentCollectionDs.containsItem(EntityValues.getId(item))) {
                        parentCollectionDs.modifyItem(item);
                    } else {
                        parentCollectionDs.addItem(item);
                    }
                }
                for (Entity item : itemsToUpdate) {
                    parentCollectionDs.modifyItem(item);
                }
                for (Entity item : itemsToDelete) {
                    parentCollectionDs.removeItem(item);
                }
                // after repeated edit of new items the parent datasource can contain items-to-create which are deleted
                // in this datasource, so we need to delete them
                Collection<Entity> parentItemsToCreate = ((DatasourceImplementation) parentCollectionDs).getItemsToCreate();
                for (Entity createdItem : new ArrayList<>(parentItemsToCreate)) {
                    if (!this.itemsToCreate.contains(createdItem)) {
                        MetaProperty inverseProp = metaProperty.getInverse();
                        // delete only if they have the same master item
                        if (inverseProp != null
                                && PersistenceHelper.isLoaded(createdItem, inverseProp.getName())
                                && Objects.equals(EntityValues.getValue(createdItem, inverseProp.getName()), masterDs.getItem())) {
                            parentCollectionDs.removeItem(createdItem);
                        }
                    }
                }
            } else {
                Entity item = null;
                if (!itemsToCreate.isEmpty()) {
                    item = itemsToCreate.iterator().next();
                } else if (!itemsToUpdate.isEmpty()) {
                    item = itemsToUpdate.iterator().next();
                } else if (!itemsToDelete.isEmpty()) {
                    item = itemsToDelete.iterator().next();
                }
                if (item != null) {
                    parentDs.setItem(item);
                }
            }
            clearCommitLists();
            modified = false;
        }
    }

    @Override
    public void refresh() {
    }

    @Override
    public void setItem(T item) {
        if (getItem() != null) {
            metadata.getTools().copy(item, getItem());
            itemsToUpdate.add(item);
        } else {
            final Entity parentItem = masterDs.getItem();
            EntityValues.setValue(parentItem, metaProperty.getName(), item);
        }
        setModified(true);
    }

    @Override
    public void invalidate() {
    }

    @Override
    public void modified(T item) {
        super.modified(item);

        if (masterDs != null) {
            ((AbstractDatasource) masterDs).modified(masterDs.getItem());
        }
    }

    @Override
    public void initialized() {
    }

    @Override
    public void valid() {
    }

    @Override
    public void committed(Set<Entity> entities) {
        Entity parentItem = masterDs.getItem();

        T prevItem = getItem();
        T newItem = null;
        for (Entity entity : entities) {
            if (entity.equals(prevItem)) {
                //noinspection unchecked
                newItem = (T) entity;
                break;
            }
        }

        // If committed set contains previousItem
        if ((parentItem != null) && newItem != null) {
            // Value changed

            boolean isModified = masterDs.isModified();

            EntityValues.setValue(parentItem, metaProperty.getName(), newItem, false);

            detachListener(prevItem);
            attachListener(newItem);

            fireItemChanged(prevItem);

            ((DatasourceImplementation) masterDs).setModified(isModified);
        } else {
            if (parentItem != null) {
                Entity newParentItem = null;
                Entity previousParentItem = null;

                // Find previous and new parent items
                Iterator<Entity> commitIter = entities.iterator();
                while (commitIter.hasNext() && (previousParentItem == null) && (newParentItem == null)) {
                    Entity commitItem = commitIter.next();
                    if (commitItem.equals(parentItem)) {
                        previousParentItem = parentItem;
                        newParentItem = commitItem;
                    }
                }
                if (previousParentItem != null) {
                    detachListener(getItem(previousParentItem));
                }
                if (newParentItem != null) {
                    attachListener(getItem(newParentItem));
                }
            }
        }
        modified = false;
        clearCommitLists();
    }

    @Override
    public Datasource getMaster() {
        return masterDs;
    }

    @Override
    public MetaProperty getProperty() {
        return metaProperty;
    }
}
