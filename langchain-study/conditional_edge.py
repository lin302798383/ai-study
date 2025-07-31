"""选择加减的图"""
from typing import TypedDict

from langchain_core.runnables.graph import MermaidDrawMethod
# 导入开始和结束节点
from langgraph.graph import StateGraph, START, END


class AgentState(TypedDict):
    operation: str
    param1: int
    param2: int
    result: int


def add_node(state: AgentState) -> AgentState:
    """执行加法"""
    state['result'] = state['param1'] + state['param2']
    return state


def sub_node(state: AgentState) -> AgentState:
    """执行减法"""
    state['result'] = state['param1'] - state['param2']
    return state


# 选择节点，返回的是边的名称
def decide_node(state: AgentState) -> AgentState:
    """决定执行加法还是减法，返回edge边"""
    if (state['operation'] == '+'):
        return "add_edge_str"
    else:
        return "sub_edge_str"


graph = StateGraph(AgentState)

graph.add_node("add_node", add_node)
graph.add_node("sub_node", sub_node)
# 这个节点是条件选择节点，在这次这个例子中，这个节点并没有对state进行任何操作，因此我们只需要原封不动把state发送就好
graph.add_node("router", lambda state: state)
graph.add_edge(START, "router")
graph.add_edge("add_node", END)
graph.add_edge("sub_node", END)

# 条件边，从router节点开始，其动作是decide_node函数，如果函数返回add_edge_str,则跳转到名字为"addNode"的节点，另外也是相同的逻辑
graph.add_conditional_edges(
    "router",
    decide_node,
    {
        "add_edge_str": "add_node",
        "sub_edge_str": "sub_node"
    }
)

app = graph.compile()
# app.get_graph().draw_mermaid_png(output_file_path="conditional_edge.png")
result = app.invoke({"operation": '+', 'param1': 10, 'param2': 1})
print(result['result'])
