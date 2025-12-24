/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.Caideyipi;

import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.server.Lifecycle;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.ManagedNamespaceWithLifecycle;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaObjectNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExtensionObject;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.structured.AddNodesItem;
import org.eclipse.milo.opcua.stack.core.types.structured.AddNodesResult;
import org.eclipse.milo.opcua.stack.core.types.structured.ObjectAttributes;
import org.eclipse.milo.opcua.stack.core.types.structured.VariableAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class OpcUaNameSpace extends ManagedNamespaceWithLifecycle {
  public static final String NAMESPACE_URI = "urn:apache:iotdb:opc-server";
  private final SubscriptionModel subscriptionModel;

  OpcUaNameSpace(final OpcUaServer server, final OpcUaServerBuilder builder) {
    super(server, NAMESPACE_URI);

    subscriptionModel = new SubscriptionModel(server, this);
    getLifecycleManager().addLifecycle(subscriptionModel);
    getLifecycleManager()
        .addLifecycle(
            new Lifecycle() {
              @Override
              public void startup() {
                // Do nothing
              }

              @Override
              public void shutdown() {
                getServer().shutdown();
                builder.close();
              }
            });
  }

  @Override
  public void addNodes(final AddNodesContext context, final List<AddNodesItem> nodesToAdd) {
    final List<AddNodesResult> results = new ArrayList<>(nodesToAdd.size());
    for (final AddNodesItem item : nodesToAdd) {
      // Check attributes
      final ExtensionObject attributes = item.getNodeAttributes();
      if (Objects.isNull(attributes)) {
        results.add(
            new AddNodesResult(
                new StatusCode(StatusCodes.Bad_NodeAttributesInvalid), NodeId.NULL_VALUE));
        continue;
      }

      // Check nodeId
      final Optional<NodeId> nodeId =
          item.getRequestedNewNodeId().toNodeId(getServer().getNamespaceTable());
      if (!nodeId.isPresent()) {
        results.add(
            new AddNodesResult(new StatusCode(StatusCodes.Bad_NodeIdRejected), NodeId.NULL_VALUE));
        continue;
      }
      if (getNodeManager().containsNode(nodeId.get())) {
        results.add(
            new AddNodesResult(new StatusCode(StatusCodes.Bad_NodeIdExists), NodeId.NULL_VALUE));
        continue;
      }

      // Check parent
      final Optional<NodeId> parentId =
          item.getParentNodeId().toNodeId(getServer().getNamespaceTable());
      if (!parentId.isPresent()) {
        results.add(
            new AddNodesResult(
                new StatusCode(StatusCodes.Bad_ParentNodeIdInvalid), NodeId.NULL_VALUE));
        continue;
      }
      final Optional<UaNode> parentNode =
          getServer().getAddressSpaceManager().getManagedNode(parentId.get());
      if (!parentNode.isPresent()) {
        results.add(
            new AddNodesResult(
                new StatusCode(StatusCodes.Bad_ParentNodeIdInvalid), NodeId.NULL_VALUE));
        continue;
      }

      // Check typeDefinition
      final Optional<NodeId> typeDefinition =
          item.getTypeDefinition().toNodeId(getServer().getNamespaceTable());
      if (!typeDefinition.isPresent()) {
        results.add(
            new AddNodesResult(
                new StatusCode(StatusCodes.Bad_TypeDefinitionInvalid), NodeId.NULL_VALUE));
        continue;
      }

      // Construct node
      final UaNode newNode;
      switch (item.getNodeClass()) {
        case Variable:
          final VariableAttributes variableAttributes =
              (VariableAttributes)
                  item.getNodeAttributes().decode(getServer().getSerializationContext());
          newNode =
              new UaVariableNode.UaVariableNodeBuilder(getNodeContext())
                  .setNodeId(nodeId.get())
                  .setAccessLevel(variableAttributes.getAccessLevel())
                  .setUserAccessLevel(variableAttributes.getUserAccessLevel())
                  .setBrowseName(item.getBrowseName())
                  .setDisplayName(variableAttributes.getDisplayName())
                  .setDataType(variableAttributes.getDataType())
                  .setTypeDefinition(typeDefinition.get())
                  .setValueRank(variableAttributes.getValueRank())
                  .setWriteMask(variableAttributes.getWriteMask())
                  .setUserWriteMask(variableAttributes.getUserWriteMask())
                  .setMinimumSamplingInterval(variableAttributes.getMinimumSamplingInterval())
                  .build();
          ((UaVariableNode) newNode)
              .setValue(
                  new DataValue(
                      variableAttributes.getValue(),
                      StatusCode.GOOD,
                      new DateTime(0),
                      new DateTime()));
          break;
        case Object:
          final ObjectAttributes objectAttributes =
              (ObjectAttributes)
                  item.getNodeAttributes().decode(getServer().getSerializationContext());
          if (typeDefinition.get().equals(Identifiers.FolderType)) {
            newNode =
                new UaFolderNode(
                    getNodeContext(),
                    nodeId.get(),
                    item.getBrowseName(),
                    objectAttributes.getDisplayName());
            break;
          }
          newNode =
              new UaObjectNode.UaObjectNodeBuilder(getNodeContext())
                  .setNodeId(nodeId.get())
                  .setBrowseName(item.getBrowseName())
                  .setDisplayName(objectAttributes.getDisplayName())
                  .setTypeDefinition(typeDefinition.get())
                  .setWriteMask(objectAttributes.getWriteMask())
                  .setUserWriteMask(objectAttributes.getUserWriteMask())
                  .build();
          break;
        default:
          results.add(
              new AddNodesResult(
                  new StatusCode(StatusCodes.Bad_NodeClassInvalid), NodeId.NULL_VALUE));
          continue;
      }

      // Link reference
      getNodeManager().addNode(newNode);
      parentNode
          .get()
          .addReference(
              new Reference(
                  parentNode.get().getNodeId(),
                  item.getReferenceTypeId(),
                  newNode.getNodeId().expanded(),
                  true));
      results.add(new AddNodesResult(StatusCode.GOOD, newNode.getNodeId()));
    }

    context.success(results);
  }

  @Override
  public void onDataItemsCreated(final List<DataItem> dataItems) {
    subscriptionModel.onDataItemsCreated(dataItems);
  }

  @Override
  public void onDataItemsModified(final List<DataItem> dataItems) {
    subscriptionModel.onDataItemsModified(dataItems);
  }

  @Override
  public void onDataItemsDeleted(final List<DataItem> dataItems) {
    subscriptionModel.onDataItemsDeleted(dataItems);
  }

  @Override
  public void onMonitoringModeChanged(final List<MonitoredItem> monitoredItems) {
    subscriptionModel.onMonitoringModeChanged(monitoredItems);
  }
}
