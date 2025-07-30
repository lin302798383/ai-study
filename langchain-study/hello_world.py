"""一个简单的图，接受一个名字，输出对这个人的招呼"""


from typing import TypedDict

from langgraph.graph import StateGraph


# 定义状态变量
class AgentState(TypedDict):
    """问候语"""
    msg: str


# 节点，接受状态参数进行操作后，返回状态参数
def greeting_node(state: AgentState) -> AgentState:
    """打一个招呼"""
    state['msg'] = "你好！" + state['msg']
    return state


# 创建图，参数为状态的定义
graph = StateGraph(AgentState)
# 添加节点
graph.add_node("greeter", greeting_node)

# 开始节点
graph.set_entry_point("greeter")
# 结束节点
graph.set_finish_point("greeter")
# 编译图
app = graph.compile()

# 执行图
result = app.invoke({"msg": "小林"})

print(result['msg'])
