package com.procurement.access.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeType

class JsonPathParser {
    companion object {
        fun parse(json: String): Map<String, String> {
            val nodes = json.toNode()
            val valueByPath = mutableMapOf<String, String>()
            parsing(nodes, "$", valueByPath)
            return valueByPath
        }

        private fun parsing(nodes: JsonNode, prefix: String, valueByPath: MutableMap<String, String>) {
            for ((name, node) in nodes.fields()) {
                when {
                    node.isObject -> parsing(node, "$prefix['$name']", valueByPath)
                    node.isArray -> {
                        if (node.size() > 0) {
                            val innerNode = node[0]
                            when (innerNode.nodeType) {
                                JsonNodeType.ARRAY,
                                JsonNodeType.OBJECT -> node.forEachIndexed { index, jsonNode ->
                                    parsing(jsonNode, "$prefix['$name'][$index]", valueByPath)
                                }

                                else -> {
                                    for (index in 0 until node.size()) {
                                        val value: String = parseLeaf(node[index])
                                        valueByPath["$prefix['$name'][$index]"] = value
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        val value: String = parseLeaf(node)
                        valueByPath["$prefix['$name']"] = value
                    }
                }
            }
        }

        private fun parseLeaf(node: JsonNode): String {
            return when (node.nodeType) {
                JsonNodeType.BOOLEAN -> if (node.asBoolean()) "true" else "false"
                JsonNodeType.NUMBER -> node.numberValue().toString()
                JsonNodeType.STRING -> "\"${node.asText()}\""
                else -> throw IllegalArgumentException("Invalid node type.")
            }
        }
    }
}
