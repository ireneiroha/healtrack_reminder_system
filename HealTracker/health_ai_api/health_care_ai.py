from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_core.messages import HumanMessage
from langchain_core.output_parsers import StrOutputParser
from langchain_huggingface import HuggingFaceEndpoint, ChatHuggingFace
import os
from dotenv import load_dotenv

load_dotenv()

llm = HuggingFaceEndpoint(
    repo_id="mistralai/Mistral-7B-Instruct-v0.2",
    task="conversational",
    huggingfacehub_api_token=os.getenv("HUGGINGFACEHUB_API_TOKEN"),
    max_new_tokens=124,
    temperature=0.7,
)

chat_model = ChatHuggingFace(llm=llm)

def get_health_care_assistance(user_input):
    prompt = ChatPromptTemplate.from_messages([
        ("system", "You are an AI that answers health queries and provide assistance."),
        MessagesPlaceholder("msg")
    ])
    chain = prompt | chat_model | StrOutputParser()
    return chain.invoke({"msg":[HumanMessage(content=user_input)]})

